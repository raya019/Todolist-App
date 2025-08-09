package app.service.impl;

import app.entity.RefreshToken;
import app.entity.User;
import app.model.RegisterRequest;
import app.model.ResponseToken;
import app.model.UserLogin;
import app.repository.RefreshTokenRepository;
import app.repository.UserRepository;
import app.service.AuthService;
import app.service.JwtService;
import app.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceImplTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private RefreshTokenRepository refreshTokenRepository;
    @MockBean
    private HttpServletRequest request;
    @MockBean
    private HttpServletResponse response;
    @Autowired
    private AuthService authService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user(){
        User user = new User();
        user.setName("adjie");
        user.setEmail("test123@gmail.com");
        user.setPassword(passwordEncoder.encode("rahasia123"));

        return user;
    }

    @Test
    void failedRegister() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(Boolean.TRUE);

        var requestUser = RegisterRequest.builder()
                .name("adjie")
                .email("adjie123@gmail.com")
                .password("rahasia123")
                .build();


        assertThrows(ResponseStatusException.class, () -> {
            authService.register(requestUser);
        }, "Email Sudah di Gunakan");
    }

    @Test
    void successRegister() {
        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);

        var requestUser = RegisterRequest.builder()
                .name("adjie")
                .email("test123@gmail.com")
                .password("rahasia123")
                .build();

        authService.register(requestUser);

        verify(userRepository,times(1)).save(any(User.class));
    }

    @Test
    void successLogin() {
        var responseToken = ResponseToken.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        var authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user());
        when(refreshTokenService.generateToken(any(User.class))).thenReturn(responseToken);

        var loginRequest = UserLogin.builder()
                .email("test123@gmail.com")
                .password("rahasia123")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();

        var login = authService.login(loginRequest, response);

        assertNotNull(login);
        assertEquals("access-token", login.get("accessToken"));
    }

    @Test
    void failedLoginCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Incorrect username or password"));

        var loginRequest = UserLogin.builder()
                .email("test123451@gmail.com")
                .password("rahasia")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThrows(BadCredentialsException.class,
                () -> authService.login(loginRequest,response),
                "Masukkan email dan password");

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(refreshTokenService, never()).generateToken(any(User.class));
        verify(refreshTokenService, never()).checkIsLogout(any(User.class));
    }

    @Test
    void successRefreshToken() {
        var user = user();

        var accessToken = "access-token";
        var oldToken = "dummy-refresh-token";
        var newToken = "new-refresh-token";

        var refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiredAt(LocalDateTime.now().plus(300000, ChronoUnit.MILLIS));
        refreshToken.setRefreshToken(oldToken);
        refreshToken.setIsLogout(false);

        var cookie = new Cookie("refreshToken",oldToken);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        when(jwtService.extractUsername(accessToken)).thenReturn(user.getEmail());
        when(jwtService.extractUsername(oldToken)).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByRefreshTokenAndIsLogoutFalse(oldToken)).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessTokens(user)).thenReturn("jwt-token");
        when(jwtService.generateRefreshTokens(user)).thenReturn(newToken);

        var responseToken = authService.refreshToken(request,response);
        assertNotNull(responseToken);
        assertEquals("jwt-token", responseToken.get("accessToken"));
    }

    @Test
    void failedCookieNotFound() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + "accessToken");
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(ResponseStatusException.class,
                () ->authService.refreshToken(request,response),
                "invalid");
    }

    @Test
    void failedUserNotFound() {
        var accessToken = "access-token";
        var oldToken = "dummy-refresh-token";

        var cookie = new Cookie("refreshToken",oldToken);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        when(jwtService.extractUsername(accessToken)).thenReturn("john123@gmail.com");
        when(jwtService.extractUsername(oldToken)).thenReturn("john123@gmail.com");

        when(userRepository.findByEmail(user().getEmail())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () ->authService.refreshToken(request,response),
                "invalid Token1");
    }

    @Test
    void failedTokenHasLogout() {
        var accessToken = "access-token";
        var oldToken = "dummy-refresh-token";

        var cookie = new Cookie("refreshToken",oldToken);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + accessToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        when(jwtService.extractUsername(accessToken)).thenReturn("john123@gmail.com");
        when(jwtService.extractUsername(oldToken)).thenReturn("john456@gmail.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user()));
        when(refreshTokenRepository.findByRefreshTokenAndIsLogoutFalse(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () ->authService.refreshToken(request,response),
                "invalid2");
    }

    @Test
    void successLogout() {
        var user = user();
        var refreshToken = new RefreshToken();
        refreshToken.setIsLogout(false);
        refreshToken.setRefreshToken("refresh-token");
        refreshToken.setUser(user);

        var cookie = new Cookie("refreshToken", "refresh-token");

        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.of(refreshToken));


        authService.logout(request,response);
        verify(userRepository, never()).save(user);
    }

    @Test
    void failedLogoutEmptyCookie() {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(request.getCookies()).thenReturn(new Cookie[]{});

        assertThrows(ResponseStatusException.class,
                () -> authService.logout(request,response),
                "Unauthorized");


        verify(refreshTokenRepository, never()).findByRefreshToken(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void failedLogoutNotFoundRefreshToken(){
        var cookie = new Cookie("refreshToken", "refresh-token");

        when(request.getHeader("Authorization")).thenReturn("token");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(refreshTokenRepository.findByRefreshToken(anyString())).thenReturn(Optional.empty());


        assertThrows(ResponseStatusException.class,
                () -> authService.logout(request,response),
                "Unauthorized");

        verify(userRepository, never()).save(any());
    }
}