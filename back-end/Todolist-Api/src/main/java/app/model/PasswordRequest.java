package app.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordRequest {

    @NotBlank(message = "Password Tidak Boleh Kosong")
    @Size(min = 8, message = "Password Minimal 8 Karakter")
    private String oldPassword;
    @NotBlank(message = "Password Tidak Boleh Kosong")
    @Size(min = 8, message = "Password Minimal 8 Karakter")
    private String newPassword;
    @NotBlank(message = "Password Tidak Boleh Kosong")
    @Size(min = 8, message = "Password Minimal 8 Karakter")
    private String confirmPassword;
}
