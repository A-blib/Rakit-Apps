package com.aris.templateapp.upload.publish;

import java.io.IOException;

/** Mengunduh file library dari CDN (dipisah agar test bisa memakai pengganti tanpa internet). */
public interface LibraryFetcher {

    /** @throws IOException jika gagal diunduh, bukan HTTP 200, atau lebih besar dari {@code maxBytes} */
    byte[] fetch(String url, long maxBytes) throws IOException;
}
