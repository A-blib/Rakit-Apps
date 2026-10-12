package com.aris.templateapp.feedback;

import com.aris.templateapp.common.response.PageResponse;
import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.feedback.dto.CreateFeedbackRequest;
import com.aris.templateapp.feedback.dto.FeedbackDetailResponse;
import com.aris.templateapp.feedback.dto.FeedbackMessageRequest;
import com.aris.templateapp.feedback.dto.FeedbackSummaryResponse;
import com.aris.templateapp.feedback.dto.UnreadCountResponse;
import com.aris.templateapp.monitoring.ClientPlatform;
import com.aris.templateapp.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Kritik & saran", description = "Pengirim (pembuat website / provider) mengirim dan membaca balasan tim")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
@Validated
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final CurrentUser currentUser;

    @Operation(summary = "Kirim kritik & saran",
            description = "Maks PORTAL_FEEDBACK_DAILY_LIMIT per 24 jam (429 TOO_MANY_ATTEMPTS). Sumber (android/web) "
                    + "dibaca dari header X-OrgSpace-Client.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackDetailResponse create(@Valid @RequestBody CreateFeedbackRequest request, HttpServletRequest http) {
        String source = ClientPlatform.of(http) == ClientPlatform.WEB ? "web" : "android";
        return feedbackService.create(currentUser.id(), request, source);
    }

    @Operation(summary = "Kritik & saran milikku", description = "Terbaru di atas. page mulai 0.")
    @GetMapping
    public PageResponse<FeedbackSummaryResponse> mine(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                      @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return feedbackService.mine(currentUser.id(), page, size);
    }

    @Operation(summary = "Jumlah percakapan dengan balasan tim yang belum dibaca")
    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(feedbackService.unreadForSender(currentUser.id()));
    }

    @Operation(summary = "Buka percakapan", description = "Menghapus tanda balasan baru. 404 juga untuk milik orang lain.")
    @GetMapping("/{id}")
    public FeedbackDetailResponse detail(@PathVariable UUID id) {
        return feedbackService.openAsSender(currentUser.id(), id);
    }

    @Operation(summary = "Tanggapi balasan tim",
            description = "Feedback resolved dibuka lagi (in_progress). 409 FEEDBACK_CLOSED jika sudah ditutup.")
    @PostMapping("/{id}/messages")
    public FeedbackDetailResponse reply(@PathVariable UUID id, @Valid @RequestBody FeedbackMessageRequest request) {
        return feedbackService.replyAsSender(currentUser.id(), id, request.body());
    }
}
