package com.Sojval.Auth.Security;


import com.Sojval.Auth.Entity.Provider;
import com.Sojval.Auth.Entity.RefreshToken;
import com.Sojval.Auth.Entity.User;
import com.Sojval.Auth.Repository.RefreshTokenRepository;
import com.Sojval.Auth.Repository.UserRepository;
import com.Sojval.Auth.Service.CookieService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            throw new ServletException("Invalid OAuth2 authentication");
        }

        String registrationId = token.getAuthorizedClientRegistrationId();

        if (!"google".equals(registrationId)) {
            throw new ServletException("Unsupported OAuth2 provider");
        }

        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        if (email == null || email.isBlank()) {
            throw new ServletException("Email not provided by Google");
        }

        logger.info("Google login for: {}", email);

        // Find the database user or create one
        User person = userRepository.findByEmail(email);

        if (person == null) {
            person = User.builder()
                    .email(email)
                    .name(name)
                    .image(picture)
                    .provider(Provider.GOOGLE)
                    .build();

            person = userRepository.save(person);
        }

        logger.info("Google ID received: {}", googleId);

        // Generate a unique refresh-token identifier
        String jti = UUID.randomUUID().toString();

        long refreshExpirationMs = jwtService
                .getJwtProperties()
                .getRefreshExpirationMs();

        Instant now = Instant.now();

        // Create and persist the refresh-token record
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setJti(jti);
        refreshTokenEntity.setUser(person);
        refreshTokenEntity.setRevoked(false);
        refreshTokenEntity.setCreated(now);
        refreshTokenEntity.setExpires(
                now.plusMillis(refreshExpirationMs));

        refreshTokenRepository.save(refreshTokenEntity);

        // Generate JWTs using the database user
        String accessToken =
                jwtService.generateAccessToken(person);

        String refreshToken =
                jwtService.generateRefreshToken(person, jti);

        // Attach refresh JWT as an HttpOnly cookie
        cookieService.attachRefreshCookie(
                response,
                refreshToken,
                Math.toIntExact(
                        jwtService.getJwtProperties().getRefreshExpirationMs() / 1000
                )
        );

        // Return access token to the client
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(accessToken);
    }
}
