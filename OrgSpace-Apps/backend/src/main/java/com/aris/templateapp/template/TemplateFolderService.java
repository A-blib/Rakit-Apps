package com.aris.templateapp.template;

import com.aris.templateapp.common.exception.ApiException;
import com.aris.templateapp.common.exception.ErrorCode;
import com.aris.templateapp.provider.ProviderAccess;
import com.aris.templateapp.template.dto.FolderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Folder di "Template Anda": buat, ganti nama, hapus, dan pindahkan template ke folder (ala Google Drive). */
@Service
@RequiredArgsConstructor
public class TemplateFolderService {

    /** Batas wajar agar daftar folder tetap ringan dimuat sekaligus (tanpa paginasi). */
    static final int MAX_FOLDERS = 100;

    private final ProviderAccess providerAccess;
    private final TemplateFolderRepository folderRepository;
    private final ProviderTemplateQueries queries;

    @Transactional(readOnly = true)
    public List<FolderResponse> list(UUID userId) {
        providerAccess.requireActive(userId);
        return queries.folders(userId);
    }

    @Transactional
    public FolderResponse create(UUID userId, String rawName) {
        providerAccess.requireActive(userId);
        String name = rawName.trim();
        if (folderRepository.countByProviderId(userId) >= MAX_FOLDERS) {
            throw new ApiException(ErrorCode.FOLDER_LIMIT_REACHED);
        }
        if (folderRepository.existsByProviderIdAndNameIgnoreCase(userId, name)) {
            throw new ApiException(ErrorCode.FOLDER_NAME_TAKEN);
        }
        TemplateFolder folder = folderRepository.save(new TemplateFolder(userId, name));
        return new FolderResponse(folder.getId(), folder.getName(), 0, folder.getUpdatedAt());
    }

    @Transactional
    public FolderResponse rename(UUID userId, UUID folderId, String rawName) {
        providerAccess.requireActive(userId);
        TemplateFolder folder = owned(userId, folderId);
        String name = rawName.trim();
        if (folderRepository.existsByProviderIdAndNameIgnoreCaseAndIdNot(userId, name, folderId)) {
            throw new ApiException(ErrorCode.FOLDER_NAME_TAKEN);
        }
        folder.rename(name);
        return new FolderResponse(folder.getId(), folder.getName(), queries.countInFolder(folderId),
                folder.getUpdatedAt());
    }

    /** Template di dalam folder tidak ikut terhapus; database mengembalikannya ke halaman utama (ON DELETE SET NULL). */
    @Transactional
    public void delete(UUID userId, UUID folderId) {
        providerAccess.requireActive(userId);
        folderRepository.delete(owned(userId, folderId));
    }

    /** @param folderId null = keluarkan dari folder */
    @Transactional
    public void moveTemplate(UUID userId, UUID templateId, UUID folderId) {
        providerAccess.requireActive(userId);
        if (folderId != null) {
            owned(userId, folderId);
        }
        if (!queries.moveToFolder(userId, templateId, folderId)) {
            throw new ApiException(ErrorCode.NOT_FOUND);
        }
    }

    private TemplateFolder owned(UUID userId, UUID folderId) {
        return folderRepository.findByIdAndProviderId(folderId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Folder tidak ditemukan."));
    }
}
