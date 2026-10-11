package com.Sojval.Auth.Security;

import com.Sojval.Auth.Entity.User;
import com.Sojval.Auth.Repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("api/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Check if Authorization header exists
        if (header != null && header.startsWith("Bearer ")) {

            // Remove "Bearer "
            String token = header.substring(7);
            if(!jwtService.isAccessTokenValid(token)){
                filterChain.doFilter(request, response);
                return;
            }
            try {

                // Parse and validate JWT
                Jwt<?, Claims> parsedJwt =
                        jwtService.parseToken(token);

                Claims payload = parsedJwt.getPayload();


                // Get subject
                String subject = payload.getSubject();

                // Subject contains User ID
                Long userId = Long.parseLong(subject);

                // Find user
                userRepository.findById(userId)
                        .ifPresent(user -> {
                            if(!user.isEnabled()){
                                try{
                                    filterChain.doFilter(request,response);
                                } catch (ServletException | IOException e) {
                                    throw new RuntimeException(e);
                                }
                                return;
                            }
                            Collection<? extends GrantedAuthority> authorities =
                                   user.getRoles( )== null ? List.of():user.getRoles().stream()
                                                                .map(role -> new SimpleGrantedAuthority(role.getName()))
                                                                .collect(Collectors.toList());

                            // Create authentication
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user,
                                            null,
                                            authorities
                                    );
                            if(SecurityContextHolder.getContext().getAuthentication() == null){
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                            }
                        });

            } catch (ExpiredJwtException | NumberFormatException e) {

                request.setAttribute("exception", e);

            } catch (JwtException e) {

               request.setAttribute("exception", e);

            } catch (Exception e) {
                request.setAttribute("exception", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}