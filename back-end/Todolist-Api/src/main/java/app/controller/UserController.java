package app.controller;

import app.entity.User;
import app.model.PasswordRequest;
import app.model.UpdateUserRequest;
import app.model.UserResponse;
import app.model.WebResponse;
import app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/user/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(path = "current",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> getUser(@AuthenticationPrincipal User user) {
        UserResponse userResponse = userService.getUser(user);
        return WebResponse.<UserResponse>builder().data(userResponse).build();
    }

    @PatchMapping(path = "current",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<UserResponse> updateUser(@AuthenticationPrincipal User user, @RequestBody UpdateUserRequest updateUserRequest) {
        UserResponse userResponse = userService.updateUser(user, updateUserRequest);
        return WebResponse.<UserResponse>builder().data(userResponse).message("Update User Success").build();
    }

    @PostMapping(path = "change-password",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> changePassword(@AuthenticationPrincipal User user, @RequestBody PasswordRequest passwordRequest) {
        userService.changePassword(user, passwordRequest);
        return WebResponse.<String>builder().message("Change Password Success").build();
    }
}
