package com.aris.templateapp.feedback.dto;

/** Angka untuk tab/badge di halaman feedback admin. {@code unread} = ada pesan pengirim yang belum dibaca tim. */
public record FeedbackCountsResponse(long all, long newCount, long inProgress, long resolved, long closed, long unread) {
}
