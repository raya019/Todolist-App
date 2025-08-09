package app.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateAccessTokens(UserDetails userDetails);
    String generateRefreshTokens(UserDetails userDetails);
    boolean isAccessTokenJwtValid(String token, UserDetails userDetails);
    String extractUsername(String token);
}
