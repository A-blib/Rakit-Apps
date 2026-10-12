package com.aris.templateapp.upload.marking;

import com.aris.templateapp.common.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** "Hubungkan ke profil organisasi": jenis isian harus cocok dengan kolom profil (alur-fitur-upload.md 15.3). */
class MarkingValidatorProfileTest {

    private static final Map<String, Set<Integer>> IDS = Map.of("index.html", Set.of(1, 2, 3, 4));

    @Test
    void matchingTypesAreAccepted() {
        assertThatCode(() -> MarkingValidator.validate(marking(
                field("nama", "text", 1, "short_name"),
                field("logo", "image", 2, "logo"),
                field("ig", "button", 3, "instagram"),
                field("surel", "link", 4, "email")), IDS, Set.of())).doesNotThrowAnyException();
    }

    @Test
    void logoOnTextIsRejected() {
        assertThatThrownBy(() -> MarkingValidator.validate(marking(field("nama", "text", 1, "logo")), IDS, Set.of()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("tidak cocok");
    }

    @Test
    void emailOnButtonAndUnknownKeyAreRejected() {
        assertThatThrownBy(() -> MarkingValidator.validate(marking(field("e", "button", 1, "email")), IDS, Set.of()))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> MarkingValidator.validate(marking(field("x", "text", 1, "rekening")), IDS, Set.of()))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("tidak dikenal");
    }

    private static MarkingData marking(MarkingData.Field... fields) {
        return new MarkingData(List.of(new MarkingData.Page("index.html", "Beranda")), List.of(), List.of(fields),
                List.of());
    }

    private static MarkingData.Field field(String key, String type, int tplId, String profileKey) {
        return new MarkingData.Field(key, key, type, null, null, false, 1, null, List.of(), null,
                List.of(new MarkingData.Element("index.html", tplId, List.of("mobile", "desktop"))), profileKey, null);
    }
}
