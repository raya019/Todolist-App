package app.service.impl;

import app.entity.RefreshToken;
import app.entity.User;
import app.model.RegisterRequest;
import app.model.UserLogin;
import app.model.UserResponse;
import app.repository.RefreshTokenRepository;
import app.repository.UserRepository;
import app.service.AuthService;
import app.service.JwtService;
import app.service.RefreshTokenService;
import app.util.ValidationUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Value("${security.jwt.refresh-token}")
    private Long expRefreshToken;
    @Value("${security.time.cookie}")
    private int expCookie;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ValidationUtil validationUtil;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public void register(RegisterRequest registerRequest) {
        validationUtil.validate(registerRequest);

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email Already Exists");
        }

        User users = new User();
        users.setName(registerRequest.getName());
        users.setEmail(registerRequest.getEmail());
        users.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(users);
    }

    @Override
    @Transactional
    public Map<String, String> login(UserLogin userLogin, HttpServletResponse httpServletResponse) {
        validationUtil.validate(userLogin);

        var authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLogin.getEmail(), userLogin.getPassword()));

        var user = (User) authenticate.getPrincipal();

        refreshTokenService.checkIsLogout(user);

        var token = refreshTokenService.generateToken(user);

        Cookie cookie = new Cookie("refreshToken", token.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(expCookie);
        httpServletResponse.addCookie(cookie);

        return Map.of("accessToken", token.getAccessToken());
    }

    @Override
    @Transactional
    public Map<String, String> refreshToken(HttpServletRequest req, HttpServletResponse res) {
        var refreshToken = Arrays.stream(req.getCookies())
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        var usernameRefreshToken = jwtService.extractUsername(refreshToken);

        var user = userRepository.findByEmail(usernameRefreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unauthorized"));

        var token = refreshTokenRepository.findByRefreshTokenAndIsLogoutFalse(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        token.setIsLogout(Boolean.TRUE);

        var generateRefreshTokens = jwtService.generateRefreshTokens(user);
        var newToken = new RefreshToken();
        newToken.setUser(user);
        newToken.setRefreshToken(generateRefreshTokens);
        newToken.setExpiredAt(LocalDateTime.now().plus(expRefreshToken, ChronoUnit.MILLIS));
        newToken.setIsLogout(Boolean.FALSE);

        refreshTokenRepository.saveAll(List.of(token,newToken));

        var accessTokens = jwtService.generateAccessTokens(user);

        var cookie  = new Cookie("refreshToken", generateRefreshTokens);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(expCookie);
        res.addCookie(cookie);

        return Map.of("accessToken", accessTokens);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        var cookieValue = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals("refreshToken"))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        var refreshToken = refreshTokenRepository.findByRefreshToken(cookieValue)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        refreshToken.setIsLogout(Boolean.TRUE);
        refreshTokenRepository.save(refreshToken);

        response.addCookie(clearCookie());
        SecurityContextHolder.clearContext();
    }

    private Cookie clearCookie() {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
