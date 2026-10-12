package com.aris.templateapp.data.remote.dto;

import java.util.List;

/** Satu halaman tab Website + jumlah per chip + batas website beralamat. */
public class WebsiteListDto {
    public List<WebsiteSummaryDto> items;
    public CountsDto counts;
    public int sitesUsed;
    public int sitesLimit;
    public int page;
    public boolean hasMore;

    public static class CountsDto {
        public long all;
        public long draft;
        public long online;
        public long changed;
    }
}
