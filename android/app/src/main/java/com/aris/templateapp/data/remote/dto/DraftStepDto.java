package com.aris.templateapp.data.remote.dto;

/** Body PATCH /providers/me/uploads/{id}/step. */
public class DraftStepDto {
    public final int step;

    public DraftStepDto(int step) {
        this.step = step;
    }
}
