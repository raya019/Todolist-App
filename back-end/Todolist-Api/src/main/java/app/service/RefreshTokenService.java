package app.service;

import app.entity.RefreshToken;
import app.entity.User;
import app.model.ResponseToken;

import java.util.Optional;

public interface RefreshTokenService {
    ResponseToken generateToken(User user);
    Optional<RefreshToken> verifyExpiredToken(String token);
    void checkIsLogout (User user);
}
