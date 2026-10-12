package com.aris.templateapp.organization;

import com.aris.templateapp.organization.dto.CategoriesResponse;
import com.aris.templateapp.template.ContentTag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Tag(name = "Meta", description = "Daftar bersama untuk form di app")
@RestController
public class MetaController {

    @Operation(summary = "Jenjang, kategori organisasi, dan pilihan Isi template (publik)",
            description = "Setiap kategori membawa daftar jenjang yang boleh memilihnya.")
    @GetMapping("/api/meta/categories")
    public CategoriesResponse categories() {
        return new CategoriesResponse(
                Arrays.stream(OrgLevel.values()).map(l -> new CategoriesResponse.Level(l.value(), l.label())).toList(),
                Arrays.stream(OrgCategory.values())
                        .map(c -> new CategoriesResponse.Category(c.value(), c.label(),
                                Arrays.stream(OrgLevel.values()).filter(c::allows).map(OrgLevel::value).toList()))
                        .toList(),
                Arrays.stream(ContentTag.values())
                        .map(t -> new CategoriesResponse.ContentTagOption(t.value(), t.label(), t.isCore()))
                        .toList());
    }
}
