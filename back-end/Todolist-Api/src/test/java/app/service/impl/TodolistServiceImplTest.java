package app.service.impl;

import app.entity.Todolist;
import app.entity.User;
import app.model.TodoAddRequest;
import app.model.TodoResponse;
import app.model.TodoUpdateRequest;
import app.repository.TodolistRepository;
import app.service.TodolistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class TodolistServiceImplTest {

    @Autowired
    private TodolistService todolistService;

    @MockBean
    private TodolistRepository todolistRepository;

    private User user(){
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setName("test");
        user.setPassword("test");

        return user;
    }

    private Todolist todolist(){
        Todolist todolist = new Todolist();
        todolist.setId("test");
        todolist.setTodolist("test");
        todolist.setIsDone(false);

        return todolist;
    }

    @Test
    void failedAddTodolist() {
        var user = user();
        var todoAddRequest = TodoAddRequest.builder()
                .todo("testats")
                .build();

        when(todolistRepository.existsByTodolistAndUser("testats",user))
                .thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> {
                todolistService.addTodolist(user, todoAddRequest);
        },"Todolist Already Exists");
    }

    @Test
    void successAddTodolist() {
        var user = user();

        TodoAddRequest todoAddRequest = TodoAddRequest.builder()
                .todo("test")
                .build();

        when(todolistRepository.existsByTodolistAndUser(todoAddRequest.getTodo(),user))
                .thenReturn(false);

        var todoResponse = todolistService.addTodolist(user, todoAddRequest);
        assertNotNull(todoResponse);
        assertEquals("test",todoResponse.getTodo());
        assertFalse(todoResponse.getIsDone());
    }

    @Test
    void successUpdateTodolist() {
        var user = user();

        when(todolistRepository.findByIdAndUser(todolist().getId(), user)).thenReturn(Optional.of(todolist()));
        when(todolistRepository.existsByTodolist(anyString())).thenReturn(false);
        when(todolistRepository.save(todolist()))
                .thenAnswer(invocation -> {
                    Todolist result = invocation.getArgument(0);
                    result.setIsDone(true);
                    result.setTodolist("test123466");

                    return result;
                });

        var todoUpdateRequest = TodoUpdateRequest.builder()
                .id(todolist().getId())
                .todo("test123466")
                .isDone(true)
                .build();

        var todoResponse = todolistService.updateTodolist(user, todoUpdateRequest);
        assertNotNull(todoResponse);
        assertEquals(todolist().getId(), todoResponse.getId());
        assertEquals("test123466", todoResponse.getTodo());
        assertTrue(todoResponse.getIsDone());
    }

    @Test
    void failedUpdateTodolistIsNotFound() {
        var user = user();

        when(todolistRepository.findByIdAndUser(todolist().getId(), user)).thenReturn(Optional.empty());

        var todoUpdateRequest = TodoUpdateRequest.builder()
                .id(todolist().getId())
                .todo("test123466")
                .isDone(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> {
            todolistService.updateTodolist(user(),todoUpdateRequest);
        },"Todolist Not Found");

        verify(todolistRepository,never()).existsByTodolist(anyString());
        verify(todolistRepository,never()).save(any(Todolist.class));
    }

    @Test
    void failedUpdateTodolistTodoExist() {
        var user = user();

        when(todolistRepository.findByIdAndUser(todolist().getId(), user)).thenReturn(Optional.of(todolist()));
        when(todolistRepository.existsByTodolist(anyString())).thenReturn(true);

        var todoUpdateRequest = TodoUpdateRequest.builder()
                .id(todolist().getId())
                .todo("test123466")
                .isDone(true)
                .build();

        assertThrows(ResponseStatusException.class, () -> {
            todolistService.updateTodolist(user(),todoUpdateRequest);
        },"Todolist Already Exists");

        verify(todolistRepository,never()).save(any(Todolist.class));
    }

    @Test
    void successDeleteTodolist() {
        var user = user();
        when(todolistRepository.findByIdAndUser(todolist().getId(),user))
                .thenReturn(Optional.of(todolist()));

        todolistService.deleteTodolist(todolist().getId(), user);
        verify(todolistRepository, times(1)).delete(any(Todolist.class));
    }

    @Test
    void FailedDeleteTodolist() {
        when(todolistRepository.findByIdAndUser(todolist().getId(),user()))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            todolistService.deleteTodolist(todolist().getId(), user());
        },"Todolist Not Found");

        verify(todolistRepository, never()).delete(any(Todolist.class));
    }

    @Test
    void successDeleteAllTodolistByUser() {
        var user = user();

        var todolist1 = todolist();
        todolist1.setTodolist("test1");
        var todolist2 = todolist();
        todolist2.setTodolist("test12");
        var todolist3 = todolist();
        todolist3.setTodolist("test123");

        todolistRepository.saveAll(List.of(todolist1,todolist2,todolist3));

        todolistService.deleteAllTodolistByUser(user);
        verify(todolistRepository, times(1)).deleteAllByUser(user);
    }

    @Test
    void emptyGetTodolist() {
        when(todolistRepository.findAllByUser(any(User.class)))
                .thenReturn(List.of());
        List<TodoResponse> allByUser = todolistService.getAllTodoByUser(user());
        assertTrue(allByUser.isEmpty());
    }

    @Test
    void getAllTodolist() {
        var user = user();

        var todolist1 = todolist();
        todolist1.setTodolist("test1");
        var todolist2 = todolist();
        todolist2.setTodolist("test12");
        var todolist3 = todolist();
        todolist3.setTodolist("test123");

        when(todolistRepository.findAllByUser(user))
                .thenReturn(List.of(todolist1,todolist2,todolist3));

        var allByUser = todolistService.getAllTodoByUser(user);
        assertFalse(allByUser.isEmpty());
       assertEquals(3,allByUser.size());
    }

    @Test
    void getAllTodolistOrderByDone() {
        var user = user();

        var todolist1 = todolist();
        todolist1.setTodolist("test1");
        var todolist2 = todolist();
        todolist2.setTodolist("test12");
        todolist2.setIsDone(true);
        var todolist3 = todolist();
        todolist3.setTodolist("test123");

        var done = Sort.by(Sort.Order.desc("isDone"));
        when(todolistRepository.findAllByUser(user,done))
                .thenReturn(List.of(todolist2,todolist1,todolist3));

        var allByUser = todolistService.getAllTodoOrderByDone(user);
        assertFalse(allByUser.isEmpty());
        assertEquals(3,allByUser.size());
        assertTrue(allByUser.get(0).getIsDone());
    }

    @Test
    void getAllTodolistOrderByName() {
        var user = user();

        var todolist1 = todolist();
        todolist1.setTodolist("btest1");
        var todolist2 = todolist();
        todolist2.setTodolist("atest12");
        todolist2.setIsDone(true);
        var todolist3 = todolist();
        todolist3.setTodolist("ctest123");

        var todolist = Sort.by(Sort.Order.asc("todolist"));
        when(todolistRepository.findAllByUser(user,todolist))
                .thenReturn(List.of(todolist2,todolist1,todolist3));

        var allByUser = todolistService.getAllTodoOrderByName(user);
        assertFalse(allByUser.isEmpty());
        assertEquals(3,allByUser.size());
        assertEquals("atest12",allByUser.get(0).getTodo());
        assertEquals("btest1",allByUser.get(1).getTodo());
        assertEquals("ctest123",allByUser.get(2).getTodo());
    }
}