package app.service.impl;

import app.entity.User;
import app.repository.RefreshTokenRepository;
import app.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtServiceImplTest {

    @Autowired
    private JwtService jwtService;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    private User createUser () {
        var user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail("john@gmail.com");
        user.setName("name");
        user.setPassword("password");

        return user;
    }

    @Test
    void generateAccessTokens() {

        var tokens = jwtService.generateAccessTokens(createUser());

        assertNotNull(tokens);
    }

    @Test
    void generateRefreshTokens() {

        var accessToken = jwtService.generateAccessTokens(createUser());
        var refreshToken = jwtService.generateRefreshTokens(createUser());

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNotEquals(accessToken,refreshToken);
    }

    @Test
    void isAccessTokenJwtValid() {
        var accessToken = jwtService.generateAccessTokens(createUser());

        var isValid = jwtService.isAccessTokenJwtValid(accessToken, createUser());

        assertTrue(isValid);
    }

    @Test
    void extractUsername() {
        var accessToken = jwtService.generateAccessTokens(createUser());
        var refreshToken = jwtService.generateRefreshTokens(createUser());

        var usernameAccessToken = jwtService.extractUsername(accessToken);
        var usernameRefreshToken = jwtService.extractUsername(refreshToken);

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertNotEquals(accessToken,refreshToken);

        assertEquals("john@gmail.com",usernameAccessToken);
        assertEquals("john@gmail.com",usernameRefreshToken);
    }

}