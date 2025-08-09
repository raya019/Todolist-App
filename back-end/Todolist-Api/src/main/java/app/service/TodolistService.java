package app.service;

import app.entity.User;
import app.model.TodoAddRequest;
import app.model.TodoResponse;
import app.model.TodoUpdateRequest;

import java.util.List;

public interface TodolistService {
    TodoResponse addTodolist(User user, TodoAddRequest todolist);

    TodoResponse updateTodolist(User user,TodoUpdateRequest todoUpdateRequest);

    void deleteTodolist(String id, User user);

    void deleteAllTodolistByUser(User user);

    List<TodoResponse> getAllTodoByUser(User user);

    List<TodoResponse> getAllTodoOrderByDone(User user);

    List<TodoResponse> getAllTodoOrderByName(User user);


}
