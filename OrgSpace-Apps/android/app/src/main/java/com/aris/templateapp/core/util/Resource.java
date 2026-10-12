package com.aris.templateapp.core.util;

import com.aris.templateapp.data.model.ApiError;

/**
 * Status data yang sedang dimuat untuk ditampilkan layar: LOADING, SUCCESS, atau ERROR.
 * ViewModel menaruh Resource di LiveData; layar cukup memeriksa {@link #getStatus()}
 * untuk memilih tampilan (loading, isi, atau pesan error + tombol coba lagi).
 */
public class Resource<T> {

    public enum Status { LOADING, SUCCESS, ERROR }

    private final Status status;
    private final T data;
    private final ApiError error;

    private Resource(Status status, T data, ApiError error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> Resource<T> loading() {
        return new Resource<>(Status.LOADING, null, null);
    }

    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(ApiError error) {
        return new Resource<>(Status.ERROR, null, error);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }
}
