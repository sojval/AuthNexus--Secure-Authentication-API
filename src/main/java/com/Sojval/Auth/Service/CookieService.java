package com.Sojval.Auth.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
public class CookieService {
    private final String refreshTokenCookieName;
    private final boolean cookieHttpOnly;
    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String cooieSameSite;
    private final String cooieMaxAge;

    public CookieService(@Value("${app.cookie.refresh-token-name}") String refreshTokenCookieName,
                         @Value("${app.cookie.refresh-token-http-only}") boolean cookieHttpOnly,
                         @Value("${app.cookie.refresh-token-secure}") boolean cookieSecure,
                         @Value("${app.cookie.refresh-token-domain}")String cookieDomain,
                         @Value("${app.cookie.refresh-token-same-site}")String cooieSameSite,
                         @Value("${app.cookie.refresh-token-max-age}")String cooieMaxAge) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cooieSameSite = cooieSameSite;
        this.cooieMaxAge = cooieMaxAge;
    }

    public void attachRefreshCookie(
            HttpServletResponse response,
            String value,
            int maxAge
    ) {
        var responseCookieBuilder = ResponseCookie
                .from(refreshTokenCookieName, value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cooieSameSite);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            responseCookieBuilder.domain(cookieDomain);
        }

        ResponseCookie responseCookie = responseCookieBuilder.build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                responseCookie.toString()
        );
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        var builder = ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0)
                .httpOnly(cookieHttpOnly)
                .path("/")
                .secure(cookieSecure)
                .sameSite(cooieSameSite);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        ResponseCookie responseCookie = builder.build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                responseCookie.toString()
        );
    }
    public void addStoreHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL,"no-store");
        response.setHeader("Pragma","no-cache");
    }
}
