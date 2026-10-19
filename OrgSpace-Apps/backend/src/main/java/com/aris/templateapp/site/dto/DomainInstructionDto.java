package com.aris.templateapp.site.dto;

public record DomainInstructionDto(
        String type,    // CNAME, A, TXT
        String host,    // misal "www", "@", "_orgspace-verify.www"
        String value,   // misal "custom.orgspace.site", "103.175.216.42", "token..."
        String note     // catatan penjelasan untuk user
) {}
