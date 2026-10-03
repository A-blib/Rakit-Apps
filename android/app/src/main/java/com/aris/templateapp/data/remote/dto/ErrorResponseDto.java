package com.aris.templateapp.data.remote.dto;

import java.util.List;
import java.util.Map;

/** Bentuk JSON error dari backend: { code, message, fieldErrors?, existingMethods?, linkToken? }. */
public class ErrorResponseDto {
    public String code;
    public String message;
    public Map<String, String> fieldErrors;
    public List<String> existingMethods;
    public String linkToken;
}
