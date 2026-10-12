package com.aris.templateapp.upload.check;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Pembaca JavaScript/CSS yang sangat sederhana (bukan parser lengkap). Tugasnya dua:
 * <ol>
 *   <li>membuang komentar tanpa menggeser posisi/baris, agar pola berbahaya di dalam komentar tidak dihitung;</li>
 *   <li>mengecek pasangan kurung {@code () [] {}} dan tanda kutip, sebagai tanda kasar syntax error.</li>
 * </ol>
 * Karena hanya perkiraan, hasil cek syntax dipakai untuk Peringatan saja (bagian 5.6 F).
 */
final class JsScanner {

    /** Hasil scan: kode tanpa komentar (panjang sama) dan pesan error pertama (null jika tidak ada). */
    record Result(String code, String error, int errorIndex) {
    }

    // Setelah karakter/kata ini, "/" berarti awal regex (mis. x = /abc/), bukan operator bagi.
    private static final String REGEX_PREFIX_CHARS = "(,=:[!&|?{};+-*%~^<>";
    private static final Set<String> REGEX_PREFIX_WORDS = Set.of("return", "typeof", "case", "in", "of", "delete",
            "void", "throw", "new", "else", "do", "yield", "await");

    private JsScanner() {
    }

    static String stripComments(String code) {
        return scan(code, true).code();
    }

    static Result scanJs(String code) {
        return scan(code, true);
    }

    /** CSS tidak punya regex/template literal, dan komentarnya hanya {@code /* *}{@code /}. */
    static Result scanCss(String code) {
        return scan(code, false);
    }

    private static Result scan(String code, boolean js) {
        StringBuilder out = new StringBuilder(code);
        Deque<Character> stack = new ArrayDeque<>();
        Deque<Integer> positions = new ArrayDeque<>();
        // Kedalaman ${ ... } di dalam template literal; saat "}" menutup level ini, kembali ke mode string `.
        Deque<Integer> templateDepth = new ArrayDeque<>();
        int n = code.length();
        int i = 0;
        while (i < n) {
            char c = code.charAt(i);
            char next = i + 1 < n ? code.charAt(i + 1) : '\0';
            if (c == '/' && next == '*') {
                int end = code.indexOf("*/", i + 2);
                if (end < 0) {
                    return new Result(out.toString(), "Komentar /* tidak ditutup.", i);
                }
                blank(out, i, end + 2);
                i = end + 2;
            } else if (js && c == '/' && next == '/') {
                int end = code.indexOf('\n', i);
                end = end < 0 ? n : end;
                blank(out, i, end);
                i = end;
            } else if (c == '"' || c == '\'') {
                int end = skipString(code, i, c, js);
                if (end < 0) {
                    return new Result(out.toString(), "Tanda kutip " + c + " tidak ditutup.", i);
                }
                i = end;
            } else if (js && c == '`') {
                int end = skipTemplate(code, i + 1);
                if (end < 0) {
                    return new Result(out.toString(), "Tanda ` tidak ditutup.", i);
                }
                if (code.charAt(end - 1) == '{') { // berhenti di ${
                    stack.push('{');
                    positions.push(end - 1);
                    templateDepth.push(stack.size());
                }
                i = end;
            } else if (js && c == '/' && regexAllowed(code, i)) {
                int end = skipRegex(code, i);
                i = end < 0 ? i + 1 : end;
            } else if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
                positions.push(i);
                i++;
            } else if (c == ')' || c == ']' || c == '}') {
                char open = c == ')' ? '(' : c == ']' ? '[' : '{';
                if (stack.isEmpty() || stack.peek() != open) {
                    return new Result(out.toString(), "Kurung " + c + " tidak punya pasangan.", i);
                }
                boolean closesTemplate = c == '}' && !templateDepth.isEmpty() && templateDepth.peek() == stack.size();
                stack.pop();
                positions.pop();
                if (closesTemplate) {
                    templateDepth.pop();
                    int end = skipTemplate(code, i + 1);
                    if (end < 0) {
                        return new Result(out.toString(), "Tanda ` tidak ditutup.", i);
                    }
                    if (code.charAt(end - 1) == '{') {
                        stack.push('{');
                        positions.push(end - 1);
                        templateDepth.push(stack.size());
                    }
                    i = end;
                } else {
                    i++;
                }
            } else {
                i++;
            }
        }
        if (!stack.isEmpty()) {
            return new Result(out.toString(), "Kurung " + stack.peek() + " tidak ditutup.", positions.peek());
        }
        return new Result(out.toString(), null, -1);
    }

    /** @return indeks setelah kutip penutup, atau -1 jika string tidak ditutup */
    private static int skipString(String code, int start, char quote, boolean js) {
        for (int i = start + 1; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == quote) {
                return i + 1;
            } else if (c == '\n' && js) {
                return -1; // string JS biasa tidak boleh melewati baris
            }
        }
        return -1;
    }

    /** Lewati isi template literal sampai ` penutup atau ${. @return indeks setelahnya, -1 jika tidak ditutup */
    private static int skipTemplate(String code, int from) {
        for (int i = from; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '`') {
                return i + 1;
            } else if (c == '$' && i + 1 < code.length() && code.charAt(i + 1) == '{') {
                return i + 2;
            }
        }
        return -1;
    }

    private static int skipRegex(String code, int start) {
        boolean inClass = false;
        for (int i = start + 1; i < code.length(); i++) {
            char c = code.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '\n') {
                return -1;
            } else if (c == '[') {
                inClass = true;
            } else if (c == ']') {
                inClass = false;
            } else if (c == '/' && !inClass) {
                return i + 1;
            }
        }
        return -1;
    }

    private static boolean regexAllowed(String code, int slash) {
        int i = slash - 1;
        while (i >= 0 && Character.isWhitespace(code.charAt(i))) {
            i--;
        }
        if (i < 0) {
            return true;
        }
        char prev = code.charAt(i);
        if (REGEX_PREFIX_CHARS.indexOf(prev) >= 0) {
            return true;
        }
        int end = i + 1;
        while (i >= 0 && Character.isJavaIdentifierPart(code.charAt(i))) {
            i--;
        }
        return REGEX_PREFIX_WORDS.contains(code.substring(i + 1, end));
    }

    private static void blank(StringBuilder out, int from, int to) {
        for (int i = from; i < to; i++) {
            if (out.charAt(i) != '\n') {
                out.setCharAt(i, ' ');
            }
        }
    }
}
