package com.aris.templateapp.template;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.template.dto.CheckResponse;
import com.aris.templateapp.template.dto.TemplateEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Template", description = "Event dilihat/didownload dan hasil pengecekan template")
@RestController
@RequestMapping("/api/templates/{id}")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateEventService eventService;
    private final ProviderTemplateService templateService;
    private final CurrentUser currentUser;

    @Operation(summary = "Catat event dilihat/didownload (boleh tanpa login)",
            description = "Event dari pemilik template, untuk template yang tidak tayang, atau download ganda untuk project yang sama diabaikan; tetap dibalas 202.")
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void recordEvent(@PathVariable UUID id, @Valid @RequestBody TemplateEventRequest request) {
        eventService.record(id, request, currentUser.optionalId());
    }

    @Operation(summary = "Hasil pengecekan terakhir (khusus pemilik template)")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    @GetMapping("/checks/latest")
    public CheckResponse latestCheck(@PathVariable UUID id) {
        return templateService.latestCheck(currentUser.id(), id);
    }
}
