package app.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLogin {

    @NotBlank(message = "Email tidak boleh kosong")
    private String email;
    @NotBlank(message = "Password tidak boleh kosong")
    private String password;

}
