package com.Sojval.Auth.Dtos;

public record ErrorResponse(
        String message,
        int status,
        String error
) {
}
