package app.service.impl;

import app.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${security.jwt.expiration-time}")
    private Long expAccessToken;
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.refresh-token}")
    private Long expRefreshToken;

    @Override
    public String generateAccessTokens(UserDetails userDetails) {
        return buildJwt(userDetails,expAccessToken);
    }

    @Override
    public String generateRefreshTokens(UserDetails userDetails) {
        return buildJwt(userDetails,expRefreshToken);
    }

    @Override
    public boolean isAccessTokenJwtValid(String token, UserDetails userDetails) {
        var subject = extractUsername(token);
        return (subject.equals(userDetails.getUsername())) && isTokenValid(token);
    }

    @Override
    public String extractUsername(String token) {
        return readJwt(token).getPayload().getSubject();
    }

    private boolean isTokenValid(String token) {
        return !readJwt(token).getPayload().getExpiration().before(new Date());
    }

    private Jws<Claims> readJwt(String tokenJWT) {
        return Jwts.parser().verifyWith(decodeSecretKey()).build().parseSignedClaims(tokenJWT);
    }

    private String buildJwt (UserDetails userDetails,Long exp) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + exp))
                .id(UUID.randomUUID().toString())
                .signWith(decodeSecretKey())
                .compact();
    }

    private SecretKey decodeSecretKey () {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }
}
