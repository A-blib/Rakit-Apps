package com.aris.templateapp.ui.creator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.aris.templateapp.data.remote.dto.GalleryPageDto.GalleryTemplateDto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/** "Template untuk anda": kategori user dulu, dilengkapi semua kategori tanpa duplikat, maksimal 6. */
public class RecommendationsTest {

    @Test
    public void fullCategoryNeedsNoFallback() {
        assertFalse(Recommendations.needsFallback(templates("a", "b", "c", "d", "e", "f")));
        assertTrue(Recommendations.needsFallback(templates("a", "b")));
        assertTrue(Recommendations.needsFallback(null));
    }

    @Test
    public void fallbackFillsWithoutDuplicatesUpToSix() {
        List<GalleryTemplateDto> merged = Recommendations.merge(templates("u1", "u2"),
                templates("p1", "u1", "p2", "u2", "p3", "p4", "p5", "p6"));

        assertEquals(List.of("u1", "u2", "p1", "p2", "p3", "p4"), ids(merged));
    }

    @Test
    public void failedFallbackKeepsPrimary() {
        assertEquals(List.of("u1"), ids(Recommendations.merge(templates("u1"), null)));
    }

    private static List<GalleryTemplateDto> templates(String... ids) {
        List<GalleryTemplateDto> list = new ArrayList<>();
        for (String id : ids) {
            GalleryTemplateDto dto = new GalleryTemplateDto();
            dto.id = id;
            dto.name = "Template " + id;
            list.add(dto);
        }
        return list;
    }

    private static List<String> ids(List<GalleryTemplateDto> list) {
        List<String> ids = new ArrayList<>();
        for (GalleryTemplateDto dto : list) {
            ids.add(dto.id);
        }
        return ids;
    }
}
