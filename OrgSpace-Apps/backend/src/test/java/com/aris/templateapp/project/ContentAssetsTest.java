package com.aris.templateapp.project;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContentAssetsTest {

    private final UUID oldId = UUID.randomUUID();
    private final UUID newId = UUID.randomUUID();

    @Test
    void replacesAssetReferencesAtAnyDepth() {
        Map<String, Object> content = Map.of(
                "fields", Map.of("foto", Map.of("image", "asset:" + oldId, "fromProfile", false)),
                "gallery", List.of("asset:" + oldId, Map.of("image", "asset:" + oldId)));

        Map<String, Object> result = ContentAssets.remap(content, Map.of(oldId, newId));

        assertThat(result).isEqualTo(Map.of(
                "fields", Map.of("foto", Map.of("image", "asset:" + newId, "fromProfile", false)),
                "gallery", List.of("asset:" + newId, Map.of("image", "asset:" + newId))));
    }

    @Test
    void leavesOtherTextAndUnknownAssetsAlone() {
        UUID other = UUID.randomUUID();
        Map<String, Object> content = Map.of("a", "asset:" + other, "b", "asset:bukan-uuid", "c", "Halo asset:",
                "d", 40);

        assertThat(ContentAssets.remap(content, Map.of(oldId, newId))).isEqualTo(content);
    }
}
