package app.service;

import app.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface AuthService {
    void register(RegisterRequest registerRequest);
    Map<String, String> login(UserLogin userLogin, HttpServletResponse httpServletResponse);
    Map<String, String> refreshToken(HttpServletRequest req, HttpServletResponse res) ;
    void logout(HttpServletRequest request, HttpServletResponse response);
}
