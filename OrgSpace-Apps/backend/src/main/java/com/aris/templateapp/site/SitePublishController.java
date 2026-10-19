package com.aris.templateapp.site;

import com.aris.templateapp.config.OpenApiConfig;
import com.aris.templateapp.security.CurrentUser;
import com.aris.templateapp.site.dto.BuildResponse;
import com.aris.templateapp.site.dto.DirectPublishRequest;
import com.aris.templateapp.site.dto.DirectPublishResponse;
import com.aris.templateapp.site.dto.FormSubmissionResponse;
import com.aris.templateapp.site.dto.PublishSiteRequest;
import com.aris.templateapp.site.dto.SiteResponse;
import com.aris.templateapp.site.dto.SlugCheckResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Publish", description = "Fitur terbitkan website dan antrean bangun (alur-publish-website.md)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SitePublishController {

    private final SitePublishService publishService;
    private final CurrentUser currentUser;

    @Operation(summary = "Terbitkan website langsung dari editor ke ruang pribadi user")
    @PostMapping("/projects/publish-direct")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public DirectPublishResponse publishDirect(@Valid @RequestBody DirectPublishRequest request) {
        return publishService.publishDirect(currentUser.id(), request);
    }

    @Operation(summary = "Cek atau sarankan nama website (slug) sebelum terbit langsung")
    @GetMapping("/projects/publish-direct/slug")
    public SlugCheckResponse checkSlug(@RequestParam String name,
                                       @RequestParam(required = false) UUID organizationId,
                                       @RequestParam(required = false) String slug) {
        return publishService.checkSlug(currentUser.id(), name, organizationId, slug);
    }

    @Operation(summary = "Terbitkan website ke alamat publik orgspace.app/{slug}")
    @PostMapping("/projects/{projectId}/publish")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BuildResponse publish(@PathVariable UUID projectId,
                                 @Valid @RequestBody(required = false) PublishSiteRequest request) {
        String slug = request != null ? request.slug() : null;
        return publishService.publish(projectId, currentUser.id(), slug);
    }

    @Operation(summary = "Cek status antrean bangun")
    @GetMapping("/builds/{buildId}")
    public BuildResponse getBuild(@PathVariable UUID buildId) {
        return publishService.getBuild(buildId);
    }

    @Operation(summary = "Detail situs yang diterbitkan")
    @GetMapping("/sites/{id}")
    public SiteResponse getSite(@PathVariable UUID id) {
        return publishService.getSite(id, currentUser.id());
    }

    @Operation(summary = "Matikan sementara website (unpublish)")
    @PostMapping("/sites/{id}/unpublish")
    public SiteResponse unpublish(@PathVariable UUID id) {
        return publishService.unpublish(id, currentUser.id());
    }

    @Operation(summary = "Hidupkan lagi website (republish)")
    @PostMapping("/sites/{id}/republish")
    public SiteResponse republish(@PathVariable UUID id) {
        return publishService.republish(id, currentUser.id());
    }

    @Operation(summary = "Detail situs berdasarkan ID project")
    @GetMapping("/projects/{projectId}/site")
    public SiteResponse getSiteByProject(@PathVariable UUID projectId) {
        return publishService.getSiteByProject(projectId, currentUser.id());
    }

    @Operation(summary = "Daftar kiriman formulir pengunjung")
    @GetMapping("/projects/{projectId}/submissions")
    public List<FormSubmissionResponse> getSubmissions(@PathVariable UUID projectId,
                                                       @RequestParam(required = false) String formName) {
        return publishService.getSubmissions(projectId, currentUser.id(), formName);
    }
}
