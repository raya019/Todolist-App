package app.controller;

import app.model.RegisterRequest;
import app.model.UserLogin;
import app.model.WebResponse;
import app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/auth/")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> register(@RequestBody RegisterRequest RequestRegister) {
        authService.register(RequestRegister);
        return WebResponse.<String>builder().message("Success Register").build();
    }

    @PostMapping(path = "login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Map<String, String>> login(@RequestBody UserLogin userLogin, HttpServletResponse httpServletResponse) {
        var login = authService.login(userLogin,httpServletResponse);
        return WebResponse.<Map<String, String>>builder().data(login).message("Berhasil Login").build();
    }

    @PostMapping(path = "refresh",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<Map<String, String>> refreshToken(HttpServletRequest req, HttpServletResponse res) {
        var responseToken = authService.refreshToken(req,res);
        return WebResponse.<Map<String, String>>builder().data(responseToken).build();
    }

    @PostMapping(path = "logout",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> logout(HttpServletResponse response, HttpServletRequest request) {
        authService.logout(request, response);
        return WebResponse.<String>builder().message("Logout Success").build();
    }
}
