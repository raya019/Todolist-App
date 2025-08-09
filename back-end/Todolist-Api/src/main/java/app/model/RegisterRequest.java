package app.model;

import jakarta.validation.constraints.Email;
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
public class RegisterRequest {
    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(min = 5 ,max = 30, message = "panjang Nama minmal 5 dan maximal 30 character")
    private String name;

    @NotBlank(message = "Email tidak boleh kosong")
    @Size(max = 30, message = "panjang Email maximal 30 character")
    @Email
    private String email;

    @NotBlank(message = "Password tidak boleh kosong")
    @Size(min = 8, message = "panjang Password minimal 8 character")
    private String password;
}
