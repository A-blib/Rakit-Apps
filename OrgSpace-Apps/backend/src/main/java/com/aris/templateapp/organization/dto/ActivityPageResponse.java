package com.aris.templateapp.organization.dto;

import java.util.List;

public record ActivityPageResponse(List<ActivityResponse> items, int page, boolean hasMore) {
}
