package app.service.impl;

import app.entity.Todolist;
import app.entity.User;
import app.model.TodoAddRequest;
import app.model.TodoResponse;
import app.model.TodoUpdateRequest;
import app.repository.TodolistRepository;
import app.service.TodolistService;
import app.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodolistServiceImpl implements TodolistService {
    private final TodolistRepository todolistRepository;
    private final ValidationUtil validationUtil;

    @Override
    @Transactional
    public TodoResponse addTodolist(User user, TodoAddRequest todolist) {
        validationUtil.validate(todolist);

        if (todolistRepository.existsByTodolistAndUser(todolist.getTodo(),user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todolist Already Exists");
        }

        Todolist todo = new Todolist();
        todo.setTodolist(todolist.getTodo());
        todo.setIsDone(false);
        todo.setUser(user);

        todolistRepository.save(todo);

        return todoResponse(todo);
    }

    @Override
    @Transactional
    public TodoResponse updateTodolist(User user, TodoUpdateRequest todoUpdateRequest) {

        validationUtil.validate(todoUpdateRequest);

        var todolist = todolistRepository.findByIdAndUser(todoUpdateRequest.getId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todolist Not Found"));

        todolist.setTodolist(todoUpdateRequest.getTodo());
        todolist.setIsDone(todoUpdateRequest.getIsDone());

        todolistRepository.save(todolist);

        return todoResponse(todolist);
    }

    @Override
    @Transactional
    public void deleteTodolist(String id,User user) {
        var todolist = todolistRepository.findByIdAndUser(id,user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todolist Not Found"));

        todolistRepository.delete(todolist);
    }

    @Override
    @Transactional
    public void deleteAllTodolistByUser(User user) {
        todolistRepository.deleteAllByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getAllTodoByUser(User user) {
        var todolist = todolistRepository.findAllByUser(user);
        return todolist.stream().map(this::todoResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getAllTodoOrderByDone(User user) {
        var done = Sort.by(Sort.Order.desc("isDone"));
        var todolist = todolistRepository.findAllByUser(user,done);
        return todolist.stream().map(this::todoResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getAllTodoOrderByName(User user) {
        var name = Sort.by(Sort.Order.asc("todolist"));
        var todolist = todolistRepository.findAllByUser(user,name);
        return todolist.stream().map(this::todoResponse).toList();
    }

    private TodoResponse todoResponse(Todolist todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .todo(todo.getTodolist())
                .isDone(todo.getIsDone())
                .build();
    }
}
