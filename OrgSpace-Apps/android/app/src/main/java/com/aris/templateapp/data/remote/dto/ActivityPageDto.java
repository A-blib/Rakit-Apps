package com.aris.templateapp.data.remote.dto;

import java.util.List;
import java.util.Map;

/** GET /organizations/{id}/activities?page= (20 per halaman, terbaru dulu). */
public class ActivityPageDto {
    public List<Item> items;
    public int page;
    public boolean hasMore;

    public static class Item {
        public String id;
        public String actorName;
        public String action;
        public Map<String, Object> metadata;
        public String createdAt;
    }
}
