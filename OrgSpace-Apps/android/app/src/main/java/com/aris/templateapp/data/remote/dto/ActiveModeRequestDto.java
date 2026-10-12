package com.aris.templateapp.data.remote.dto;

/** Body PATCH /users/me/active-mode: "creator" atau "provider". */
public class ActiveModeRequestDto {
    public final String mode;

    public ActiveModeRequestDto(String mode) {
        this.mode = mode;
    }
}
