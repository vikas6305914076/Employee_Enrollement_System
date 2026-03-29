package com.company.ems.auth.jwt;

import java.time.Instant;

public record JwtToken(String accessToken, Instant expiresAt) {
}
