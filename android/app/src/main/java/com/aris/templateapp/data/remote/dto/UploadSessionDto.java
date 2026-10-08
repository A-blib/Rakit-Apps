package com.aris.templateapp.data.remote.dto;

/** Posisi upload per potongan: server sudah menerima {@code receivedSize} dari {@code totalSize} byte. */
public class UploadSessionDto {
    public String id;
    public String fileName;
    public long totalSize;
    public long receivedSize;
    public int chunkSize;
}
