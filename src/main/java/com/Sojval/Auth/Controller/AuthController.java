
package com.Sojval.Auth.Controller;

import com.Sojval.Auth.Dtos.LoginRequest;
import com.Sojval.Auth.Dtos.RefreshTokenRequest;
import com.Sojval.Auth.Dtos.TokenResponse;
import com.Sojval.Auth.Dtos.UserDto;
import com.Sojval.Auth.Entity.RefreshToken;
import com.Sojval.Auth.Entity.User;
import com.Sojval.Auth.Repository.RefreshTokenRepository;
import com.Sojval.Auth.Repository.UserRepository;
import com.Sojval.Auth.Security.JwtService;
import com.Sojval.Auth.Service.AuthService;
import com.Sojval.Auth.Service.CookieService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        // 1. Authenticate user
        authenticate(loginRequest);

        // 2. Fetch user
        User user = userRepository.findByEmail(loginRequest.email());

        if (user == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // 3. Create and save refresh token record
        String jti = UUID.randomUUID().toString();
        Instant now = Instant.now();

        RefreshToken refreshToken = new RefreshToken(
                jti,
                user,
                now,
                now.plusMillis(
                        jwtService.getJwtProperties()
                                .getRefreshExpirationMs()
                ),
                false
        );

        refreshTokenRepository.save(refreshToken);

        // 4. Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(
                user, refreshToken.getJti()
        );


        // use cookie service to attach refresh token in cookie
        cookieService.attachRefreshCookie(response,refresh,(int)jwtService.getJwtProperties().getRefreshExpirationMs());
        cookieService.addStoreHeaders(response);
        // 5. Map user DTO
        UserDto userDto = modelMapper.map(user, UserDto.class);

        // 6. Return response
        TokenResponse tokenResponse = TokenResponse.of(
                accessToken,
                refresh,
                jwtService.getJwtProperties().getExpirationMs(),
                "Bearer",
                userDto
        );

        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
            @RequestBody UserDto userDto) {

        return ResponseEntity.ok(
                authService.registerUser(userDto)
        );
    }

    // access and refresh token renew krne lie api
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletResponse response,
            HttpServletRequest request

    ) {
        String refreshToken = readRefreshTokenFromRequest(body,request).orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if(!jwtService.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        String jti = jwtService.getJti(refreshToken);
          Long userId = jwtService.getUserId(refreshToken);



        RefreshToken storedRefreshToken  = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if(storedRefreshToken.isRevoked()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        if (!storedRefreshToken.getUser().getId().equals(userId)) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        // refresh token
        storedRefreshToken.setRevoked(true);
        String newJti = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJti);
        refreshTokenRepository.save(storedRefreshToken);
        User user = storedRefreshToken.getUser();
        RefreshToken refreshToken1 = new RefreshToken(
                newJti,user
                ,Instant.now()
                ,Instant.now().plusSeconds(jwtService.getJwtProperties().getRefreshExpirationMs())
                , false
        );
        refreshTokenRepository.save(refreshToken1);
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user, refreshToken1.getJti());
        cookieService.attachRefreshCookie(response,newRefreshToken,Math.toIntExact(
                jwtService.getJwtProperties().getRefreshExpirationMs() / 1000
        ));
        cookieService.addStoreHeaders(response);
        return ResponseEntity.ok(
                TokenResponse.of(
                        newAccessToken
                        ,newRefreshToken
                        ,jwtService.getJwtProperties().getExpirationMs()
                        ,"Bearer"
                        ,modelMapper.map(user, UserDto.class)
                )
        );
    }
    @PostMapping("logout")
    public ResponseEntity<Void> logout(HttpServletRequest request ,HttpServletResponse response) {
        readRefreshTokenFromRequest(null,request).ifPresent(token ->{
            try{
                if(jwtService.isRefreshToken(token)) {
                    String jti  = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jti).ifPresent(tokenResponse -> {
                        tokenResponse.setRevoked(true);
                        refreshTokenRepository.save(tokenResponse);
                    });
                }
            }
            catch (JwtException ignored){}
        });

        // user cookieUtil
        cookieService.clearRefreshCookie(response);
        cookieService.addStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
        // 1. prefer reading refresh token from cookie
        if(request.getCookies() != null) {
            Optional<String> fromCookie = Arrays.stream(
                    request.getCookies()

            ).filter(c -> cookieService.getRefreshTokenCookieName()
                            .equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v ->!v.isBlank())
                    .findFirst();

            if(fromCookie.isPresent()){
                return fromCookie;
            }
        }


        if(body != null && body.refreshToken() != null && !body.refreshToken().isEmpty()) {
            return Optional.of(body.refreshToken());
        }

        String refreshToken = request.getHeader("X-refresh-token");
        if(refreshToken != null && !refreshToken.isEmpty()) {
            return Optional.of(refreshToken);
        }
        // authorization = Bearer <token>
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authHeader != null && authHeader.regionMatches(true,0,"Bearer", 0, 7)) {
            String candidate = authHeader.substring(7).trim();
            if(!candidate.isEmpty()){
                try{
                    if(jwtService.isRefreshToken(candidate)){
                        return Optional.of(candidate);
                    }
                }
                catch(Exception ignored){}
            }
        }
        return Optional.empty();
    }
}