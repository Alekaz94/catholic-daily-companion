/*
 * Copyright (c) 2025 Alexandros Kazalis
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.alexandros.dailycompanion.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException notFoundException, HttpServletRequest req, Principal principal) {
        logError("ENTITY_NOT_FOUND", notFoundException, req, principal);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFoundException.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException badRequestException, HttpServletRequest req, Principal principal) {
        logError("BAD_REQUEST", badRequestException, req, principal);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(badRequestException.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException accessDeniedException, HttpServletRequest req, Principal principal) {
        logError("ACCESS_DENIED", accessDeniedException, req, principal);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(accessDeniedException.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFound(UsernameNotFoundException usernameNotFoundException, HttpServletRequest req, Principal principal) {
        logError("USERNAME_NOT_FOUND", usernameNotFoundException, req, principal);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(usernameNotFoundException.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException methodArgumentNotValidException, HttpServletRequest req, Principal principal) {
        String errorMessage = methodArgumentNotValidException
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logError("VALIDATION_ERROR", methodArgumentNotValidException, req, principal);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException illegalArgumentException, HttpServletRequest req, Principal principal) {
        logError("ILLEGAL_ARGUMENT", illegalArgumentException, req, principal);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(illegalArgumentException.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException illegalStateException, HttpServletRequest req, Principal principal) {
        logError("ILLEGAL_STATE", illegalStateException, req, principal);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(illegalStateException.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<String> handleFileNotFound(FileNotFoundException fileNotFoundException, HttpServletRequest req, Principal principal) {
        logError("FILE_NOT_FOUND", fileNotFoundException, req, principal);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(fileNotFoundException.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException dataIntegrityViolationException, HttpServletRequest req, Principal principal) {
        logError("DATA_INTEGRITY_VIOLATION", dataIntegrityViolationException, req, principal);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(dataIntegrityViolationException.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpectedException(Exception exception, HttpServletRequest req, Principal principal) {
        logError("UNEXPECTED_ERROR", exception, req, principal);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred.");
    }

    private void logError(String type, Exception ex, HttpServletRequest req, Principal principal) {
        logError(type, ex, req, principal, ex.getMessage());
    }

    private void logError(String type, Exception ex, HttpServletRequest req, Principal principal, String details) {
        String username = (principal != null) ? principal.getName() : "anonymous";
        logger.error("{} | user={} | method={} | path={} | ip={} | message={}",
                type,
                username,
                req.getMethod(),
                req.getRequestURI(),
                req.getRemoteAddr(),
                details
        );
    }
}
