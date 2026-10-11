package com.Sojval.Auth.Dtos;

import java.time.OffsetDateTime;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        OffsetDateTime timestamp
) {
    public static ApiError of(int status, String error, String message,String path,OffsetDateTime timestamp) {
        return  new ApiError(status, error, message, path,OffsetDateTime.now());
    }
}
