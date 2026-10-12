package com.aris.templateapp.project.dto;

/** @param confirmName wajib sama dengan nama website jika website pernah diterbitkan */
public record DeleteWebsiteRequest(String confirmName) {
}
