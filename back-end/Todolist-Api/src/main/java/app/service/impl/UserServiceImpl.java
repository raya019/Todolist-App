package app.service.impl;

import app.entity.User;
import app.model.PasswordRequest;
import app.model.UpdateUserRequest;
import app.model.UserResponse;
import app.repository.RefreshTokenRepository;
import app.repository.UserRepository;
import app.service.UserService;
import app.util.ValidationUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ValidationUtil validationUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(User user) {
        return userResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(User user,UpdateUserRequest updateUserRequest) {
        validationUtil.validate(updateUserRequest);

        if (Objects.nonNull(updateUserRequest.getName())) {
            user.setName(updateUserRequest.getName());
            userRepository.save(user);
        }

        return userResponse(user);
    }

    @Override
    @Transactional
    public void changePassword (User user, PasswordRequest passwordRequest) {
        validationUtil.validate(passwordRequest);

        if (!passwordEncoder.matches(passwordRequest.getOldPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old Password No Match");
        }

        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New Password with Confirm Password No Match");
        }

        user.setPassword(passwordEncoder.encode(passwordRequest.getNewPassword()));

        userRepository.save(user);
    }

    private UserResponse userResponse(User user){
        return UserResponse
                .builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    private Cookie clearCookie() {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        return cookie;
    }
}
