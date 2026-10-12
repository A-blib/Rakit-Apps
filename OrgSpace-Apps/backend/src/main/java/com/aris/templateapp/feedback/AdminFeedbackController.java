package com.aris.templateapp.feedback;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.common.response.PageResponse;
import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.feedback.dto.FeedbackCountsResponse;
import com.aris.templateapp.feedback.dto.FeedbackDetailResponse;
import com.aris.templateapp.feedback.dto.FeedbackMessageRequest;
import com.aris.templateapp.feedback.dto.FeedbackStatusRequest;
import com.aris.templateapp.feedback.dto.FeedbackSummaryResponse;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Admin: Kritik & saran", description = "Tim internal membaca, membalas, dan mengubah status (khusus admin)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/admin/feedback")
@RequiredArgsConstructor
@Validated
public class AdminFeedbackController {

    private final FeedbackService feedbackService;
    private final CurrentUser currentUser;

    @Operation(summary = "Daftar kritik & saran",
            description = "status: new/in_progress/resolved/closed; role: creator/provider; category: bug/saran/"
                    + "pertanyaan/keluhan/lainnya; unread=true untuk yang belum dibaca tim; q cari judul/isi/pengirim.")
    @GetMapping
    public PageResponse<FeedbackSummaryResponse> list(@RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String role,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) Boolean unread,
                                                      @RequestParam(required = false) String q,
                                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        check(status, "new|in_progress|resolved|closed", "status");
        check(role, "creator|provider", "role");
        check(category, "bug|saran|pertanyaan|keluhan|lainnya", "category");
        return feedbackService.adminPage(new FeedbackService.AdminFilter(status, role, category, q, unread), page, size);
    }

    @Operation(summary = "Jumlah per status + belum dibaca")
    @GetMapping("/counts")
    public FeedbackCountsResponse counts() {
        return feedbackService.counts();
    }

    @Operation(summary = "Buka percakapan", description = "Menghapus tanda belum dibaca tim.")
    @GetMapping("/{id}")
    public FeedbackDetailResponse detail(@PathVariable UUID id) {
        return feedbackService.openAsStaff(id);
    }

    @Operation(summary = "Balas", description = "Status new otomatis menjadi in_progress; pengirim mendapat tanda balasan baru.")
    @PostMapping("/{id}/messages")
    public FeedbackDetailResponse reply(@PathVariable UUID id, @Valid @RequestBody FeedbackMessageRequest request) {
        return feedbackService.replyAsStaff(currentUser.id(), id, request.body());
    }

    @Operation(summary = "Ubah status")
    @PatchMapping("/{id}")
    public FeedbackDetailResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody FeedbackStatusRequest request) {
        return feedbackService.changeStatus(id, request.status());
    }

    private static void check(String value, String allowed, String name) {
        if (value != null && !value.matches(allowed)) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, name + " tidak dikenal: " + value);
        }
    }
}
