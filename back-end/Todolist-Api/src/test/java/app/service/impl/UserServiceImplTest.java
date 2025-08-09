package app.service.impl;

import app.entity.RefreshToken;
import app.entity.User;
import app.model.PasswordRequest;
import app.model.UpdateUserRequest;
import app.model.UserResponse;
import app.repository.RefreshTokenRepository;
import app.repository.UserRepository;
import app.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class  UserServiceImplTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private HttpServletRequest request;

    @MockBean
    private HttpServletResponse response;

    private User user(){
        User user = new User();
        user.setName("johnnn");
        user.setEmail("test123@gmail.com");
        user.setPassword(passwordEncoder.encode("rahasia123"));

        return user;
    }

    @Test
    void successUpdateUser() {
        var user = user();

        when(userRepository.save(user)).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setName("johnap");
            return savedUser;
        });

        var updateUser = UpdateUserRequest.builder()
                .name("johnap")
                .build();

        UserResponse userResponse = userService.updateUser(user, updateUser);

        assertNotNull(userResponse);
        assertEquals("test123@gmail.com", userResponse.getEmail());
        assertNotNull(userResponse.getName());
        assertEquals("johnap", userResponse.getName());
    }

    @Test
    void successChangePasswordUser() {
        var user = user();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setPassword("rahasia321");
            return savedUser;
        });

        var updateUser = PasswordRequest.builder()
                .oldPassword("rahasia123")
                .newPassword("rahasia321")
                .confirmPassword("rahasia321")
                .build();

        userService.changePassword(user, updateUser);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void failedChangePasswordUserWrongOldPassword() {
        var user = user();

        var updateUser = PasswordRequest.builder()
                .oldPassword("rahasia123444")
                .newPassword("rahasia321")
                .confirmPassword("rahasia321")
                .build();


        assertThrows(ResponseStatusException.class, () -> {
            userService.changePassword(user, updateUser);
        },"Old Password No Match");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void failedChangePasswordUserWrongConfirmPassword() {
        var user = user();

        var updateUser = PasswordRequest.builder()
                .oldPassword("rahasia123")
                .newPassword("rahasia321")
                .confirmPassword("rahasia3214454")
                .build();


        assertThrows(ResponseStatusException.class, () -> {
            userService.changePassword(user, updateUser);
        },"New Password with Confirm Password No Match");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void failedChangePasswordUserWrongNewPassword() {
        var user = user();

        var updateUser = PasswordRequest.builder()
                .oldPassword("rahasia123")
                .newPassword("rahasia3214454")
                .confirmPassword("rahasia32112")
                .build();


        assertThrows(ResponseStatusException.class, () -> {
            userService.changePassword(user, updateUser);
        },"New Password with Confirm Password No Match");

        verify(userRepository, never()).save(any(User.class));
    }

}