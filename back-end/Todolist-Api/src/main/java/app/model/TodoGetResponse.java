package app.model;


import java.util.List;

import app.entity.Todolist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoGetResponse {

    private List<Todolist> data;

}
