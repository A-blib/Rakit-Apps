package com.aris.templateapp.help;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.help.dto.HelpArticleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@Tag(name = "Panduan", description = "Artikel Panduan per aturan pengecekan (boleh tanpa login)")
@RestController
@RequestMapping("/api/help/articles")
@RequiredArgsConstructor
public class HelpController {

    private final HelpArticleRepository repository;

    @Operation(summary = "Artikel Panduan untuk satu kode aturan",
            description = "404 NOT_FOUND jika aturan belum punya artikel; app lalu membuka halaman Panduan umum.")
    @GetMapping("/{code}")
    @Transactional(readOnly = true)
    public HelpArticleResponse article(@PathVariable String code) {
        HelpArticle a = repository.findById(code.toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Artikel untuk aturan ini belum ada."));
        return new HelpArticleResponse(a.getCode(), a.getTitle(), a.getWhy(), a.getWrongExample(), a.getRightExample(),
                a.getHowToFix(), a.getTips());
    }
}
