package com.daghan.catalog.application.dto;

public record LoginResponse(
        String token,
        String username,
        String role) {
}
