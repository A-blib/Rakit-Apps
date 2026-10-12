package com.aris.templateapp.upload.dto;

import com.aris.templateapp.organization.OrgCategory;
import com.aris.templateapp.template.TemplateStatus;
import com.aris.templateapp.upload.check.TechInfo;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Isi draft untuk langkah 3 (Info template) dan seterusnya.
 *
 * @param nameDuplicate true jika provider sudah punya template lain dengan nama yang sama (Peringatan, bagian 6.1)
 */
public record DraftResponse(UUID templateId, TemplateStatus status, int wizardStep, String sourceFileName,
                            Long sourceSize, String name, OrgCategory category, String description,
                            List<String> keywords, List<String> contentTags, String thumbnailUrl, String thumbnailSource, String thumbnailView,
                            TechInfo techInfo, int warningCount, int fieldCount, boolean nameDuplicate, Instant updatedAt) {
}
