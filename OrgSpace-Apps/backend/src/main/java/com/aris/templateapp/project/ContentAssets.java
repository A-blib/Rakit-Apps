package com.aris.templateapp.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rujukan gambar di isi website ({@code values.json}) berbentuk {@code "asset:{id}"}. Class ini mengganti rujukan
 * itu di seluruh isi (berapa pun dalamnya), mis. saat website diduplikat dan asetnya mendapat id baru.
 */
final class ContentAssets {

    static final String PREFIX = "asset:";

    private ContentAssets() {
    }

    static Map<String, Object> remap(Map<String, Object> content, Map<UUID, UUID> ids) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) remapValue(content, ids);
        return result;
    }

    private static Object remapValue(Object value, Map<UUID, UUID> ids) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new HashMap<>();
            map.forEach((key, item) -> copy.put(String.valueOf(key), remapValue(item, ids)));
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            list.forEach(item -> copy.add(remapValue(item, ids)));
            return copy;
        }
        if (value instanceof String text && text.startsWith(PREFIX)) {
            try {
                UUID replacement = ids.get(UUID.fromString(text.substring(PREFIX.length())));
                return replacement == null ? text : PREFIX + replacement;
            } catch (IllegalArgumentException notAnId) {
                return text;
            }
        }
        return value;
    }
}
