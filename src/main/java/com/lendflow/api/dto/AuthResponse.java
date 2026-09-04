// src/main/java/com/lendflow/api/dto/AuthResponse.java
package com.lendflow.api.dto;

public record AuthResponse(String token, Long userId, String name, String email) {}