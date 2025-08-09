package app.controller;

import app.entity.User;
import app.model.TodoAddRequest;
import app.model.TodoResponse;
import app.model.TodoUpdateRequest;
import app.model.WebResponse;
import app.service.TodolistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/todolist/")
public class TodolistController {
    private final TodolistService service;

    @PostMapping(path = "add",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TodoResponse> addTodolist(@AuthenticationPrincipal User user, @RequestBody TodoAddRequest request) {
        var todoResponse = service.addTodolist(user, request);
        return WebResponse.<TodoResponse>builder().data(todoResponse).message("Success Add Todolist").build();
    }

    @PutMapping(
            path = "update/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TodoResponse> updateTodolist(
            @AuthenticationPrincipal User user,
            @RequestBody TodoUpdateRequest todoUpdate,
            @PathVariable("id") String idString
    ) {

        todoUpdate.setId(idString);
        TodoResponse updateTodolist = service.updateTodolist(user, todoUpdate);
        return WebResponse.<TodoResponse>builder().data(updateTodolist).message("Success Update Todolist").build();
    }

    @DeleteMapping(
            path = "delete/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> deleteTodolist(@AuthenticationPrincipal User user, @PathVariable("id") String idString) {
        service.deleteTodolist(idString,user);
        return WebResponse.<String>builder().message("Success Delete Todolist").build();
    }

    @DeleteMapping(
            path = "delete-all",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> deleteALlTodolistByUser(@AuthenticationPrincipal User user) {
        service.deleteAllTodolistByUser(user);
        return WebResponse.<String>builder().message("Success Delete All Todolist").build();
    }

    @GetMapping(
            path = "get",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<TodoResponse>> getTodolist(@AuthenticationPrincipal User user) {
        var result = service.getAllTodoByUser(user);
        return WebResponse.<List<TodoResponse>>builder().data(result).build();
    }

    @GetMapping(
            path = "get-order-by-done",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<TodoResponse>> getTodolistOrderByDone(@AuthenticationPrincipal User user) {
        var result = service.getAllTodoOrderByDone(user);
        return WebResponse.<List<TodoResponse>>builder().data(result).build();
    }

    @GetMapping(
            path = "get-order-by-name",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<TodoResponse>> getTodolistOrderByName(@AuthenticationPrincipal User user) {
        var result = service.getAllTodoOrderByName(user);
        return WebResponse.<List<TodoResponse>>builder().data(result).build();
    }
}
