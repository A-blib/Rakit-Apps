package com.aris.templateapp.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

/** Gambar foto profil user (publik, seperti logo organisasi). Unggah & hapus ada di {@link UserController}. */
@Tag(name = "User", description = "Data user yang sedang login, onboarding, dan mode")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAvatarController {

    private final UserService userService;

    @Operation(summary = "Gambar foto profil user (publik)")
    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> avatar(@PathVariable UUID id) {
        // URL foto membawa ?v=<waktu>, jadi gambar boleh disimpan lama di cache.
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)))
                .contentType(MediaType.IMAGE_PNG)
                .body(new FileSystemResource(userService.avatarFile(id)));
    }
}
