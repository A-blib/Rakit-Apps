package com.aris.templateapp.upload.check;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Pembaca ZIP kecil buatan sendiri, dipakai alih-alih {@code java.util.zip.ZipFile} karena pengecekan butuh
 * informasi yang tidak disediakan ZipFile: tanda <b>symlink</b> (atribut Unix), tanda <b>dikunci password</b>,
 * dan kontrol penuh atas jumlah byte yang diekstrak (mencegah <i>zip bomb</i>).
 * <p>
 * Format ZIP: di ujung file ada "End of Central Directory" yang menunjuk ke daftar isi (central directory);
 * setiap baris daftar isi menunjuk ke "local header" yang diikuti data file (disimpan apa adanya atau dikompres
 * deflate). Dokumen resmi: PKWARE APPNOTE.TXT.
 */
final class ZipArchive {

    private static final int EOCD_SIGNATURE = 0x06054b50;
    private static final int CENTRAL_SIGNATURE = 0x02014b50;
    private static final int LOCAL_SIGNATURE = 0x04034b50;
    private static final int METHOD_STORED = 0;
    private static final int METHOD_DEFLATE = 8;
    private static final long ZIP64_MARKER = 0xFFFFFFFFL;
    private static final Charset CP437 = Charset.forName("IBM437");

    /** Error format ZIP; pesannya ditampilkan ke provider. */
    static final class ZipFormatException extends Exception {
        ZipFormatException(String message) {
            super(message);
        }
    }

    /** Salah satu file di daftar isi ZIP. */
    record Entry(String name, boolean directory, boolean encrypted, boolean symlink, int method,
                 long compressedSize, long size, long crc, long localHeaderOffset) {
    }

    private final ByteBuffer data;
    private final List<Entry> entries;

    private ZipArchive(byte[] bytes) throws ZipFormatException {
        this.data = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        this.entries = readCentralDirectory();
    }

    static ZipArchive open(byte[] bytes) throws ZipFormatException {
        return new ZipArchive(bytes);
    }

    List<Entry> entries() {
        return entries;
    }

    /**
     * Mengekstrak satu file dengan batas ukuran. Ukuran di daftar isi bisa dipalsukan, jadi yang dihitung adalah
     * byte yang benar-benar keluar dari dekompresi.
     *
     * @return isi file, atau null jika melebihi {@code maxBytes}
     */
    byte[] read(Entry entry, long maxBytes) throws ZipFormatException {
        int pos = (int) entry.localHeaderOffset();
        if (pos < 0 || pos + 30 > data.limit() || data.getInt(pos) != LOCAL_SIGNATURE) {
            throw new ZipFormatException("Data file " + entry.name() + " rusak.");
        }
        int start = pos + 30 + u16(pos + 26) + u16(pos + 28);
        long end = start + entry.compressedSize();
        if (end > data.limit()) {
            throw new ZipFormatException("Data file " + entry.name() + " terpotong.");
        }
        byte[] compressed = new byte[(int) entry.compressedSize()];
        data.get(start, compressed);

        byte[] content = switch (entry.method()) {
            case METHOD_STORED -> compressed.length > maxBytes ? null : compressed;
            case METHOD_DEFLATE -> inflate(entry, compressed, maxBytes);
            default -> throw new ZipFormatException(
                    "File " + entry.name() + " memakai metode kompresi yang tidak didukung. Buat ulang ZIP dengan pengaturan standar.");
        };
        if (content != null) {
            CRC32 crc = new CRC32();
            crc.update(content);
            if (crc.getValue() != entry.crc()) {
                throw new ZipFormatException("Isi file " + entry.name() + " rusak (checksum tidak cocok).");
            }
        }
        return content;
    }

    private static byte[] inflate(Entry entry, byte[] compressed, long maxBytes) throws ZipFormatException {
        // "nowrap = true" karena data deflate di ZIP tidak memakai header zlib.
        Inflater inflater = new Inflater(true);
        try {
            inflater.setInput(compressed);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[64 * 1024];
            while (!inflater.finished()) {
                int n = inflater.inflate(buffer);
                if (n == 0 && (inflater.needsInput() || inflater.needsDictionary())) {
                    break;
                }
                out.write(buffer, 0, n);
                if (out.size() > maxBytes) {
                    return null;
                }
            }
            return out.toByteArray();
        } catch (DataFormatException e) {
            throw new ZipFormatException("Isi file " + entry.name() + " rusak.");
        } finally {
            inflater.end();
        }
    }

    private List<Entry> readCentralDirectory() throws ZipFormatException {
        int eocd = findEndOfCentralDirectory();
        int count = u16(eocd + 10);
        long cdOffset = u32(eocd + 16);
        if (cdOffset >= data.limit()) {
            throw new ZipFormatException("Daftar isi ZIP rusak.");
        }
        List<Entry> result = new ArrayList<>(count);
        int pos = (int) cdOffset;
        for (int i = 0; i < count; i++) {
            if (pos + 46 > data.limit() || data.getInt(pos) != CENTRAL_SIGNATURE) {
                throw new ZipFormatException("Daftar isi ZIP rusak.");
            }
            int madeBy = u16(pos + 4);
            int flags = u16(pos + 8);
            int method = u16(pos + 10);
            long crc = u32(pos + 16);
            long compressedSize = u32(pos + 20);
            long size = u32(pos + 24);
            int nameLength = u16(pos + 28);
            int extraLength = u16(pos + 30);
            int commentLength = u16(pos + 32);
            long externalAttributes = u32(pos + 38);
            long localOffset = u32(pos + 42);
            if (pos + 46 + nameLength + extraLength > data.limit()) {
                throw new ZipFormatException("Daftar isi ZIP rusak.");
            }
            byte[] nameBytes = new byte[nameLength];
            data.get(pos + 46, nameBytes);
            String name = decodeName(nameBytes, (flags & 0x0800) != 0);

            // ZIP64: ukuran asli disimpan di "extra field" 0x0001 jika kolom biasa bernilai 0xFFFFFFFF.
            if (size == ZIP64_MARKER || compressedSize == ZIP64_MARKER || localOffset == ZIP64_MARKER) {
                long[] zip64 = readZip64(pos + 46 + nameLength, extraLength, size, compressedSize, localOffset);
                size = zip64[0];
                compressedSize = zip64[1];
                localOffset = zip64[2];
            }

            // Byte atas "version made by" = sistem pembuat (3 = Unix); atribut Unix ada di 16 bit atas.
            boolean unix = (madeBy >> 8) == 3;
            boolean symlink = unix && ((externalAttributes >>> 16) & 0xF000) == 0xA000;
            boolean directory = name.endsWith("/") || name.endsWith("\\");
            result.add(new Entry(name, directory, (flags & 0x0001) != 0, symlink, method,
                    compressedSize, size, crc, localOffset));
            pos += 46 + nameLength + extraLength + commentLength;
        }
        return result;
    }

    private long[] readZip64(int extraStart, int extraLength, long size, long compressedSize, long localOffset) {
        int pos = extraStart;
        int end = extraStart + extraLength;
        while (pos + 4 <= end) {
            int id = u16(pos);
            int length = u16(pos + 2);
            if (id == 0x0001) {
                int field = pos + 4;
                if (size == ZIP64_MARKER && field + 8 <= end) {
                    size = data.getLong(field);
                    field += 8;
                }
                if (compressedSize == ZIP64_MARKER && field + 8 <= end) {
                    compressedSize = data.getLong(field);
                    field += 8;
                }
                if (localOffset == ZIP64_MARKER && field + 8 <= end) {
                    localOffset = data.getLong(field);
                }
                break;
            }
            pos += 4 + length;
        }
        return new long[]{size, compressedSize, localOffset};
    }

    private int findEndOfCentralDirectory() throws ZipFormatException {
        // EOCD berukuran 22 byte + komentar (maks 65535 byte), jadi dicari mundur dari ujung file.
        int min = Math.max(0, data.limit() - 22 - 65535);
        for (int pos = data.limit() - 22; pos >= min; pos--) {
            if (data.getInt(pos) == EOCD_SIGNATURE) {
                return pos;
            }
        }
        throw new ZipFormatException("File ini bukan ZIP atau ZIP-nya rusak.");
    }

    /**
     * Nama file ditulis UTF-8 jika bit 11 menyala; jika tidak, standar ZIP memakai CP437. Banyak alat menulis
     * UTF-8 tanpa menyalakan bit itu, jadi UTF-8 yang valid tetap dibaca sebagai UTF-8.
     */
    private static String decodeName(byte[] bytes, boolean utf8Flag) {
        if (utf8Flag || Texts.isValidUtf8(bytes)) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return new String(bytes, CP437);
    }

    private int u16(int pos) {
        return data.getShort(pos) & 0xFFFF;
    }

    private long u32(int pos) {
        return data.getInt(pos) & 0xFFFFFFFFL;
    }
}
