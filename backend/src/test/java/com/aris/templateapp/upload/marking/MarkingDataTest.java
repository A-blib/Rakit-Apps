package com.aris.templateapp.upload.marking;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Urutan isian mengikuti posisi di situs, bukan urutan provider menandai. */
class MarkingDataTest {

    @Test
    void fieldsAreSortedByPageThenElementPosition() {
        MarkingData data = new MarkingData(
                List.of(new MarkingData.Page("index.html", "Beranda"), new MarkingData.Page("kontak.html", "Kontak")),
                List.of(),
                List.of(field("alamat", "kontak.html", 5, 1), field("deskripsi", "index.html", 16, 2),
                        field("judul", "index.html", 14, 3)),
                List.of());

        MarkingData ordered = data.inPageOrder();

        assertThat(ordered.fields()).extracting(MarkingData.Field::key).containsExactly("judul", "deskripsi", "alamat");
        assertThat(ordered.fields()).extracting(MarkingData.Field::order).containsExactly(1, 2, 3);
    }

    private static MarkingData.Field field(String key, String page, int tplId, int order) {
        return new MarkingData.Field(key, key, "text", null, null, false, order, null, List.of(), null,
                List.of(new MarkingData.Element(page, tplId, List.of("mobile", "desktop"))));
    }
}
