package com.Sojval.Auth.Dtos;

public record TokenResponse(
        String accessToken,
        String freshToken,
        long expiresIn,
        String tokenType,
        UserDto user
) {
    public static TokenResponse of(String accessToken
            ,String freshToken
            ,long expiresIn
            ,String tokenType
            , UserDto user)
    {
        return new TokenResponse(
                accessToken
                , freshToken
                , expiresIn
                , "Bearer", user);
    }

}
