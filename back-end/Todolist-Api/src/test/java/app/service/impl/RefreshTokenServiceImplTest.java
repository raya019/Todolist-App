package app.service.impl;

import app.entity.RefreshToken;
import app.entity.User;
import app.repository.RefreshTokenRepository;
import app.repository.UserRepository;
import app.service.JwtService;
import app.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class RefreshTokenServiceImplTest {

    @MockBean
    private RefreshTokenRepository tokenRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;

    private User user () {
        var user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail("john@gmail.com");
        user.setName("name");
        user.setPassword("password");

        return user;
    }

    @Test
    void generateTokenSuccess() {

        when(jwtService.generateAccessTokens(any())).thenReturn("access-token");
        when(jwtService.generateRefreshTokens(any())).thenReturn("refresh-token");

        var generateRefreshToken = refreshTokenService.generateToken(user());
        assertNotNull(generateRefreshToken);
    }

    @Test
    void successVerifyExpiredToken() {

        var token = new RefreshToken();
        token.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        token.setRefreshToken("refresh-token");
        token.setIsLogout(false);

        when(tokenRepository.findByRefreshToken(anyString()))
                .thenReturn(Optional.of(token));

        refreshTokenService.verifyExpiredToken(token.getRefreshToken()).ifPresent( refreshToken1 -> {
            assertNotNull(refreshToken1);
            assertFalse(refreshToken1.getIsLogout());
            assertEquals(token.getExpiredAt(), refreshToken1.getExpiredAt());
            assertEquals(token.getRefreshToken(), refreshToken1.getRefreshToken());
        });
    }

    @Test
    void failedVerifyExpiredTokenNotFound() {

        when(tokenRepository.findByRefreshToken(anyString()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid Token"));

        assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.verifyExpiredToken(anyString()),
                "Invalid Token");

        verify(tokenRepository,never()).save(any(RefreshToken.class));
    }

    @Test
    void failedVerifyExpiredToken() {

        var token = new RefreshToken();
        token.setExpiredAt(LocalDateTime.now());
        token.setRefreshToken("refresh-token");
        token.setIsLogout(false);

        when(tokenRepository.findByRefreshToken(anyString()))
                .thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class,
                () -> refreshTokenService.verifyExpiredToken(token.getRefreshToken()),
                "Refresh token expired");

        verify(tokenRepository,times(1)).save(any(RefreshToken.class));
    }


}