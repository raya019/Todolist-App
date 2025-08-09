package app.service.impl;

import app.entity.RefreshToken;
import app.entity.User;
import app.model.ResponseToken;
import app.repository.RefreshTokenRepository;
import app.service.JwtService;
import app.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${security.jwt.refresh-token}")
    private Long refreshToken;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Override
    @Transactional
    public ResponseToken generateToken(User user) {
        var accessToken = jwtService.generateAccessTokens(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(jwtService.generateRefreshTokens(user));
        refreshToken.setUser(user);
        refreshToken.setIsLogout(Boolean.FALSE);
        refreshToken.setExpiredAt(LocalDateTime.now().plus(this.refreshToken, ChronoUnit.MILLIS));
        refreshTokenRepository.save(refreshToken);

        return ResponseToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }

    @Override
    @Transactional
    public Optional<RefreshToken> verifyExpiredToken(String token) {
        var tokenDB = refreshTokenRepository.findByRefreshToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token" ));

        if (tokenDB.getExpiredAt().isBefore(LocalDateTime.now())) {
            tokenDB.setIsLogout(Boolean.TRUE);
            refreshTokenRepository.save(tokenDB);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        return Optional.of(tokenDB);
    }

    @Override
    @Transactional
    public void checkIsLogout (User user) {
        refreshTokenRepository.findByUserAndIsLogoutFalse(user)
                .ifPresent(token ->{
                    token.setIsLogout(Boolean.TRUE);
                    refreshTokenRepository.save(token);
                });
    }
}
