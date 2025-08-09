package app.service;

import app.entity.User;
import app.model.PasswordRequest;
import app.model.UpdateUserRequest;
import app.model.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    UserResponse getUser(User user);
    UserResponse updateUser(User user, UpdateUserRequest updateUserRequest);
    void changePassword(User user, PasswordRequest passwordRequest);
}