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
public class TodoAddRequest {

    @NotBlank(message = "Todolist Tidak Boleh Kosong")
    @Size(max = 100, message = "panjang Todolist harus 100 character")
    private String todo;

}
