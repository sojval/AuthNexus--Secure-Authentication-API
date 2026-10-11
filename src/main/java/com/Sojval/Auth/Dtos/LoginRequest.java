package com.Sojval.Auth.Dtos;

public record LoginRequest(
        String email,
        String password
) {
}
