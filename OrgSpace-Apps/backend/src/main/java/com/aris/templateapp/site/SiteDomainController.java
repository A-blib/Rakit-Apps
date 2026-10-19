package com.aris.templateapp.site;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.site.dto.AddDomainRequest;
import com.aris.templateapp.site.dto.DomainResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Custom Domain", description = "Fitur domain kustom milik organisasi (alur-publish-website.md 7.10)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping({"/api/sites/{siteId}/domains", "/api/projects/{siteId}/domains"})
@RequiredArgsConstructor
public class SiteDomainController {

    private final SiteDomainService domainService;
    private final CurrentUser currentUser;

    @Operation(summary = "Ambil informasi domain kustom website")
    @GetMapping
    public ResponseEntity<DomainResponse> getDomain(@PathVariable UUID siteId) {
        return domainService.getDomain(siteId, currentUser.id())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @Operation(summary = "Tambahkan domain kustom (status pending)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DomainResponse addDomain(@PathVariable UUID siteId,
                                    @Valid @RequestBody AddDomainRequest request) {
        return domainService.addDomain(siteId, currentUser.id(), request);
    }

    @Operation(summary = "Periksa dan verifikasi DNS domain kustom")
    @PostMapping("/{domainId}/verify")
    public DomainResponse verifyDomain(@PathVariable UUID siteId,
                                       @PathVariable UUID domainId,
                                       @RequestParam(defaultValue = "false") boolean bypassDns) {
        return domainService.verifyDomain(siteId, domainId, currentUser.id(), bypassDns);
    }

    @Operation(summary = "Lepas domain kustom")
    @DeleteMapping("/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDomain(@PathVariable UUID siteId,
                             @PathVariable UUID domainId) {
        domainService.removeDomain(siteId, domainId, currentUser.id());
    }
}
