package com.Sojval.Auth.Exceptions;

import com.Sojval.Auth.Dtos.ApiError;
import com.Sojval.Auth.Dtos.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.Time;
import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({
        UsernameNotFoundException.class,
        BadCredentialsException.class,
        CredentialsExpiredException.class,
        ExpiredJwtException.class,
        JwtException.class,
        AuthenticationException.class
    })
    public ResponseEntity<ApiError> handleAuthException(Exception e, HttpServletRequest request){
        var api = ApiError.of(HttpStatus.BAD_REQUEST.value(),"Bad request",e.getMessage(),request.getRequestURI(), OffsetDateTime.now());
        return ResponseEntity.badRequest().body(api);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex){
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage(),404,"Resource not found"));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex){
        return ResponseEntity.status(400).body(new ErrorResponse(ex.getMessage(),400,"Illegal Argument"));
    }
}
