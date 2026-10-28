package com.aris.templateapp.site;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Pengaturan tampilan Portal Pengurus dari {@code portal.json} di folder paling luar template (keputusan Aris,
 * 28 Oktober 2026). File ini hanya memilih dari pilihan yang disediakan Portal; tidak ada kode provider yang jalan.
 * <p>
 * Satu class untuk dua tempat: cek upload memakai {@link Result#errors()} (template ditolak jika ada), render website
 * memakai {@link Result#config()} yang hanya berisi bagian yang valid, sehingga Hosting tidak perlu menebak.
 * Kontrak: artikel /docs/tampilan-portal-pengurus (V40) dan docs/dokumentasi-project.md bab 33ab.
 */
public final class PortalConfig {

    public static final String FILE_NAME = "portal.json";
    /** Pengaturan yang valid hanya beberapa KB; batas ini mencegah file besar di-parse ulang di setiap render. */
    public static final int MAX_SIZE = 64 * 1024;

    /** Ikon yang tersedia di Portal (nama ikon Lucide). Ikon baru harus ditambahkan juga di Hosting. */
    public static final Set<String> ICONS = Set.of("layout-dashboard", "inbox", "newspaper", "calendar", "users",
            "user", "trophy", "award", "star", "image", "camera", "target", "flag", "book-open", "graduation-cap",
            "briefcase", "handshake", "heart", "map-pin", "megaphone", "clipboard-list", "file-text", "folder",
            "list", "boxes", "tent", "compass", "music", "sparkles", "mail", "link");
    private static final Set<String> BUILTINS = Set.of("overview", "news", "submissions");
    private static final Set<String> IMAGE_EXT = Set.of("png", "jpg", "jpeg", "webp");
    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{6}");
    private static final Pattern SAFE_PATH = Pattern.compile("[A-Za-z0-9._-]+(/[A-Za-z0-9._-]+)*");
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final int LABEL_MAX = 40;
    private static final int TEXT_MAX = 140;
    private static final int NAV_MAX = 30;
    private static final int BOTTOM_MAX = 4;

    /** Hasil baca: {@code config} hanya berisi bagian valid (kosong jika JSON rusak), {@code errors} untuk provider. */
    public record Result(Map<String, Object> config, List<String> errors) {
    }

    private final Map<String, Map<String, String>> modules;
    private final Predicate<String> fileExists;
    private final List<String> errors = new ArrayList<>();

    private PortalConfig(Map<String, Map<String, String>> modules, Predicate<String> fileExists) {
        this.modules = modules;
        this.fileExists = fileExists;
    }

    /**
     * @param modules    modul koleksi template (nama → field → jenis), dari {@link PublicSiteController#templateModules}
     * @param fileExists apakah path relatif (mis. img/header.jpg) ada di template
     */
    public static Result read(String json, Map<String, Map<String, String>> modules, Predicate<String> fileExists) {
        PortalConfig reader = new PortalConfig(modules, fileExists);
        Map<String, Object> config = reader.root(json);
        return new Result(config, List.copyOf(reader.errors));
    }

    private Map<String, Object> root(String json) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (json.length() > MAX_SIZE) {
            errors.add("portal.json terlalu besar (maksimal " + MAX_SIZE / 1024 + " KB).");
            return out;
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            errors.add("portal.json bukan JSON yang valid: " + e.getOriginalMessage());
            return out;
        }
        if (root == null || !root.isObject()) {
            errors.add("portal.json harus berupa objek { ... }.");
            return out;
        }
        allowKeys(root, "portal.json", "version", "theme", "layout", "nav", "modules", "mobile");
        if (root.has("version") && !(root.get("version").isInt() && root.get("version").asInt() == 1)) {
            errors.add("version harus 1.");
        }
        put(out, "theme", theme(root.get("theme")));
        put(out, "layout", layout(root.get("layout")));
        Set<String> navIds = new HashSet<>();
        put(out, "nav", nav(root.get("nav"), navIds));
        put(out, "modules", moduleSettings(root.get("modules")));
        put(out, "mobile", mobile(root.get("mobile")));
        return out;
    }

    private Map<String, Object> theme(JsonNode node) {
        if (!object(node, "theme")) {
            return null;
        }
        allowKeys(node, "theme", "primary", "accent", "radius");
        Map<String, Object> out = new LinkedHashMap<>();
        for (String key : List.of("primary", "accent")) {
            if (!node.has(key)) {
                continue;
            }
            String color = node.get(key).asString("");
            if (!HEX.matcher(color).matches()) {
                errors.add("theme." + key + " harus warna hex 6 digit, mis. #1E40AF.");
            } else if (contrastWithWhite(color) < 3.0) {
                // Warna utama dipakai sebagai latar tombol berteks putih dan warna teks di atas latar putih.
                errors.add("theme." + key + " " + color + " terlalu terang: teks putih di atasnya tidak terbaca "
                        + "(kontras minimal 3:1). Pakai warna yang lebih gelap.");
            } else {
                out.put(key, color.toUpperCase(Locale.ROOT));
            }
        }
        Integer radius = intInRange(node, "radius", "theme.radius", 0, 24);
        put(out, "radius", radius);
        return out;
    }

    private Map<String, Object> layout(JsonNode node) {
        if (!object(node, "layout")) {
            return null;
        }
        allowKeys(node, "layout", "sidebar", "header");
        Map<String, Object> out = new LinkedHashMap<>();
        JsonNode sidebar = node.get("sidebar");
        if (object(sidebar, "layout.sidebar")) {
            allowKeys(sidebar, "layout.sidebar", "position", "width", "resizable");
            Map<String, Object> s = new LinkedHashMap<>();
            put(s, "position", choice(sidebar, "position", "layout.sidebar.position", Set.of("left", "right")));
            put(s, "width", intInRange(sidebar, "width", "layout.sidebar.width", 200, 360));
            put(s, "resizable", bool(sidebar, "resizable", "layout.sidebar.resizable"));
            put(out, "sidebar", s);
        }
        JsonNode header = node.get("header");
        if (object(header, "layout.header")) {
            allowKeys(header, "layout.header", "variant", "image", "title", "subtitle");
            Map<String, Object> h = new LinkedHashMap<>();
            put(h, "variant", choice(header, "variant", "layout.header.variant", Set.of("minimal", "banner")));
            put(h, "image", image(header, "image", "layout.header.image"));
            put(h, "title", text(header, "title", "layout.header.title", 60));
            put(h, "subtitle", text(header, "subtitle", "layout.header.subtitle", TEXT_MAX));
            put(out, "header", h);
        }
        return out;
    }

    private List<Object> nav(JsonNode node, Set<String> seen) {
        if (node == null) {
            return null;
        }
        if (!node.isArray()) {
            errors.add("nav harus berupa daftar [ ... ].");
            return null;
        }
        if (node.size() > NAV_MAX) {
            errors.add("nav maksimal " + NAV_MAX + " menu.");
        }
        List<Object> out = new ArrayList<>();
        int i = 0;
        for (JsonNode entry : node) {
            String where = "nav[" + i++ + "]";
            if (entry.isObject() && entry.has("group")) {
                allowKeys(entry, where, "group", "items");
                String title = text(entry, "group", where + ".group", LABEL_MAX);
                JsonNode items = entry.get("items");
                if (title == null || items == null || !items.isArray() || items.isEmpty()) {
                    errors.add(where + ": grup butuh \"group\" (judul) dan \"items\" (daftar menu).");
                    continue;
                }
                List<Object> groupItems = new ArrayList<>();
                int j = 0;
                for (JsonNode item : items) {
                    put(groupItems, navItem(item, where + ".items[" + j++ + "]", seen));
                }
                if (!groupItems.isEmpty()) {
                    out.add(Map.of("group", title, "items", groupItems));
                }
            } else {
                put(out, navItem(entry, where, seen));
            }
        }
        return out;
    }

    private Map<String, Object> navItem(JsonNode node, String where, Set<String> seen) {
        if (!object(node, where)) {
            return null;
        }
        allowKeys(node, where, "builtin", "module", "label", "icon");
        String builtin = node.has("builtin") ? node.get("builtin").asString("") : null;
        String module = node.has("module") ? node.get("module").asString("").toLowerCase(Locale.ROOT) : null;
        if ((builtin == null) == (module == null)) {
            errors.add(where + ": isi salah satu, \"builtin\" atau \"module\".");
            return null;
        }
        if (builtin != null && !BUILTINS.contains(builtin)) {
            errors.add(where + ".builtin \"" + builtin + "\" tidak dikenal. Pilihan: overview, news, submissions.");
            return null;
        }
        if (module != null && !modules.containsKey(module)) {
            errors.add(where + ".module \"" + module + "\" tidak ada di HTML template (data-ws-collection). "
                    + "Modul yang ada: " + String.join(", ", modules.keySet()) + ".");
            return null;
        }
        String id = builtin != null ? builtin : module;
        if (!seen.add(id)) {
            errors.add(where + ": \"" + id + "\" sudah ada di nav. Setiap menu hanya boleh muncul sekali.");
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put(builtin != null ? "builtin" : "module", id);
        put(out, "label", text(node, "label", where + ".label", LABEL_MAX));
        put(out, "icon", icon(node, where + ".icon"));
        return out;
    }

    private Map<String, Object> moduleSettings(JsonNode node) {
        if (!object(node, "modules")) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            String where = "modules." + entry.getKey();
            Map<String, String> fields = modules.get(name);
            if (fields == null) {
                errors.add(where + ": modul \"" + name + "\" tidak ada di HTML template.");
                continue;
            }
            JsonNode m = entry.getValue();
            if (!object(m, where)) {
                continue;
            }
            allowKeys(m, where, "label", "icon", "description", "view", "title", "subtitle", "image", "fields",
                    "groups");
            Map<String, Object> s = new LinkedHashMap<>();
            put(s, "label", text(m, "label", where + ".label", LABEL_MAX));
            put(s, "icon", icon(m, where + ".icon"));
            put(s, "description", text(m, "description", where + ".description", TEXT_MAX));
            put(s, "view", choice(m, "view", where + ".view", Set.of("tabel", "kartu")));
            put(s, "title", fieldRef(m, "title", where, fields, false));
            put(s, "subtitle", fieldRef(m, "subtitle", where, fields, false));
            put(s, "image", fieldRef(m, "image", where, fields, true));
            put(s, "fields", fieldLabels(m.get("fields"), where + ".fields", fields));
            put(s, "groups", groups(m.get("groups"), where + ".groups", fields));
            out.put(name, s);
        }
        return out;
    }

    private String fieldRef(JsonNode node, String key, String where, Map<String, String> fields, boolean image) {
        if (!node.has(key)) {
            return null;
        }
        String field = node.get(key).asString("");
        if (!fields.containsKey(field)) {
            errors.add(where + "." + key + ": field \"" + field + "\" tidak ada di modul ini. Field yang ada: "
                    + String.join(", ", fields.keySet()) + ".");
            return null;
        }
        if (image && !"gambar".equals(fields.get(field))) {
            errors.add(where + "." + key + ": field \"" + field + "\" bukan gambar (<img data-ws-field>).");
            return null;
        }
        return field;
    }

    private Map<String, Object> fieldLabels(JsonNode node, String where, Map<String, String> fields) {
        if (!object(node, where)) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String at = where + "." + entry.getKey();
            if (!fields.containsKey(entry.getKey())) {
                errors.add(at + ": field \"" + entry.getKey() + "\" tidak ada di modul ini.");
                continue;
            }
            if (!object(entry.getValue(), at)) {
                continue;
            }
            allowKeys(entry.getValue(), at, "label", "help");
            Map<String, Object> f = new LinkedHashMap<>();
            put(f, "label", text(entry.getValue(), "label", at + ".label", LABEL_MAX));
            put(f, "help", text(entry.getValue(), "help", at + ".help", TEXT_MAX));
            put(out, entry.getKey(), f);
        }
        return out;
    }

    private List<Object> groups(JsonNode node, String where, Map<String, String> fields) {
        if (node == null) {
            return null;
        }
        if (!node.isArray()) {
            errors.add(where + " harus berupa daftar [ ... ].");
            return null;
        }
        List<Object> out = new ArrayList<>();
        Set<String> used = new HashSet<>();
        int i = 0;
        for (JsonNode group : node) {
            String at = where + "[" + i++ + "]";
            if (!object(group, at)) {
                continue;
            }
            allowKeys(group, at, "title", "fields");
            String title = text(group, "title", at + ".title", LABEL_MAX);
            JsonNode list = group.get("fields");
            if (title == null || list == null || !list.isArray() || list.isEmpty()) {
                errors.add(at + ": grup butuh \"title\" dan \"fields\" (daftar nama field).");
                continue;
            }
            List<String> names = new ArrayList<>();
            for (JsonNode f : list) {
                String name = f.asString("");
                if (!fields.containsKey(name)) {
                    errors.add(at + ".fields: field \"" + name + "\" tidak ada di modul ini.");
                } else if (!used.add(name)) {
                    errors.add(at + ".fields: field \"" + name + "\" sudah masuk grup lain.");
                } else {
                    names.add(name);
                }
            }
            if (!names.isEmpty()) {
                out.add(Map.of("title", title, "fields", names));
            }
        }
        return out;
    }

    private Map<String, Object> mobile(JsonNode node) {
        if (!object(node, "mobile")) {
            return null;
        }
        allowKeys(node, "mobile", "nav", "bottom");
        Map<String, Object> out = new LinkedHashMap<>();
        put(out, "nav", choice(node, "nav", "mobile.nav", Set.of("drawer", "bottom")));
        JsonNode bottom = node.get("bottom");
        if (bottom != null) {
            if (!bottom.isArray()) {
                errors.add("mobile.bottom harus berupa daftar, mis. [\"overview\", \"agenda\"].");
            } else {
                if (bottom.size() > BOTTOM_MAX) {
                    errors.add("mobile.bottom maksimal " + BOTTOM_MAX + " menu; sisanya otomatis masuk \"Lainnya\".");
                }
                List<String> ids = new ArrayList<>();
                for (JsonNode id : bottom) {
                    String value = id.asString("").toLowerCase(Locale.ROOT);
                    if (!BUILTINS.contains(value) && !modules.containsKey(value)) {
                        errors.add("mobile.bottom: \"" + value + "\" bukan menu bawaan maupun modul template.");
                    } else if (!ids.contains(value) && ids.size() < BOTTOM_MAX) {
                        ids.add(value);
                    }
                }
                put(out, "bottom", ids);
            }
        }
        return out;
    }

    // ---------- pembaca nilai kecil ----------

    private boolean object(JsonNode node, String where) {
        if (node == null) {
            return false;
        }
        if (!node.isObject()) {
            errors.add(where + " harus berupa objek { ... }.");
            return false;
        }
        return true;
    }

    /** Kunci asing ditolak: salah ketik (mis. "posisi") lebih baik ketahuan saat upload daripada diam-diam diabaikan. */
    private void allowKeys(JsonNode node, String where, String... keys) {
        Set<String> allowed = Set.of(keys);
        for (String key : node.propertyNames()) {
            if (!allowed.contains(key)) {
                errors.add(where + ": pengaturan \"" + key + "\" tidak dikenal. Yang tersedia: "
                        + String.join(", ", keys) + ".");
            }
        }
    }

    private String choice(JsonNode node, String key, String where, Set<String> options) {
        if (!node.has(key)) {
            return null;
        }
        String value = node.get(key).asString("");
        if (!options.contains(value)) {
            errors.add(where + " \"" + value + "\" tidak tersedia. Pilihan: " + String.join(", ", options.stream().sorted().toList()) + ".");
            return null;
        }
        return value;
    }

    private Integer intInRange(JsonNode node, String key, String where, int min, int max) {
        if (!node.has(key)) {
            return null;
        }
        JsonNode value = node.get(key);
        if (!value.isInt() || value.asInt() < min || value.asInt() > max) {
            errors.add(where + " harus angka bulat " + min + "–" + max + ".");
            return null;
        }
        return value.asInt();
    }

    private Boolean bool(JsonNode node, String key, String where) {
        if (!node.has(key)) {
            return null;
        }
        if (!node.get(key).isBoolean()) {
            errors.add(where + " harus true atau false.");
            return null;
        }
        return node.get(key).asBoolean();
    }

    /** Teks polos: Portal menampilkannya sebagai teks (bukan HTML), di sini hanya dibatasi panjangnya. */
    private String text(JsonNode node, String key, String where, int max) {
        if (!node.has(key)) {
            return null;
        }
        JsonNode value = node.get(key);
        String text = value.isString() ? value.asString().strip() : "";
        if (text.isEmpty() || text.length() > max) {
            errors.add(where + " harus teks 1–" + max + " karakter.");
            return null;
        }
        return text;
    }

    private String icon(JsonNode node, String where) {
        if (!node.has("icon")) {
            return null;
        }
        String icon = node.get("icon").asString("");
        if (!ICONS.contains(icon)) {
            errors.add(where + " \"" + icon + "\" tidak tersedia. Pilihan: "
                    + String.join(", ", ICONS.stream().sorted().toList()) + ".");
            return null;
        }
        return icon;
    }

    private String image(JsonNode node, String key, String where) {
        if (!node.has(key)) {
            return null;
        }
        String path = node.get(key).asString("").strip();
        String ext = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        if (!SAFE_PATH.matcher(path).matches() || path.contains("..") || !IMAGE_EXT.contains(ext)) {
            errors.add(where + " harus path gambar relatif di dalam template (png/jpg/webp), mis. img/header.jpg.");
            return null;
        }
        if (!fileExists.test(path)) {
            errors.add(where + ": file " + path + " tidak ada di ZIP.");
            return null;
        }
        return path;
    }

    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null && !(value instanceof Map<?, ?> m && m.isEmpty())) {
            map.put(key, value);
        }
    }

    private static void put(List<Object> list, Object value) {
        if (value != null) {
            list.add(value);
        }
    }

    /** Rasio kontras WCAG antara warna hex dan putih. */
    static double contrastWithWhite(String hex) {
        double r = channel(Integer.parseInt(hex.substring(1, 3), 16));
        double g = channel(Integer.parseInt(hex.substring(3, 5), 16));
        double b = channel(Integer.parseInt(hex.substring(5, 7), 16));
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return 1.05 / (luminance + 0.05);
    }

    private static double channel(int value) {
        double c = value / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
