package app.controller;

import app.entity.Todolist;
import app.entity.User;
import app.model.*;
import app.repository.RefreshTokenRepository;
import app.repository.TodolistRepository;
import app.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TodolistControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        todolistRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        savedUser = userRepository.save(user());
    }

    @AfterEach
    void tearDown() {
        todolistRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private WebResponse<Map<String, String>> createToken(Object request) throws Exception {
        return mapper.readValue(mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(request))
                )
                .andExpectAll(
                        status().isOk()
                ).andDo(
                        result -> {
                            WebResponse<Map<String, String>> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );
                        }
                ).andReturn().getResponse().getContentAsString(), new TypeReference<>() {
        });
    }

    private User user() {
        User user = new User();
        user.setName("Test");
        user.setEmail("test@gmail.com");
        user.setPassword(passwordEncoder.encode("password"));

        return user;
    }

    private UserLogin userLogin() {
        return UserLogin.builder()
                .email("test@gmail.com")
                .password("password")
                .build();
    }

    private Todolist todolist(){
        Todolist todolist = new Todolist();
        todolist.setTodolist("Belajar Spring");
        todolist.setIsDone(false);
        todolist.setUser(savedUser);

        return todolist;
    }

    @Test
    void successAddTodolist() throws Exception {
        var token = createToken(userLogin());

        var todoAddRequest = TodoAddRequest.builder()
                .todo("Belajar Spring")
                .build();

        mockMvc.perform(
                        post("/api/todolist/add")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(todoAddRequest)))
                .andExpectAll(
                        status().isOk())
                .andDo(
                        result -> {
                            WebResponse<TodoResponse> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });

                            assertEquals("Success Add Todolist", value.getMessage());
                            assertNotNull(value.getData());
                            todolistRepository
                                    .findByIdAndUser(value.getData().getId(),savedUser)
                                    .ifPresent(todolist -> {
                                        assertEquals(value.getData().getTodo(), todolist.getTodolist());
                                        assertFalse(todolist.getIsDone());
                                    });


                        }

                );
    }

    @Test
    void failedAddTodolist() throws Exception {
        todolistRepository.save(todolist());

        var token = createToken(userLogin());

        var todoAddRequest =  TodoAddRequest.builder()
                .todo("Belajar Spring")
                .build();

        mockMvc.perform(
                        post("/api/todolist/add")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(todoAddRequest)))
                .andExpectAll(
                        status().isBadRequest())
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });
                            assertEquals("Todolist Already Exists", value.getErrors());
                        }

                );
    }

    @Test
    void successUpdateTodolist() throws Exception {
        var save = todolistRepository.save(todolist());

        var token = createToken(userLogin());

        var todoUpdateRequest =  TodoUpdateRequest.builder()
                .id(save.getId())
                .todo("Belajar Spring Security")
                .isDone(todolist().getIsDone())
                .build();

        mockMvc.perform(
                        put("/api/todolist/update/" + todoUpdateRequest.getId())
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(todoUpdateRequest))
                )
                .andExpectAll(
                        status().isOk())
                .andDo(
                        result -> {
                            WebResponse<TodoResponse> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });
                            assertNotNull(value.getData());
                            assertEquals("Success Update Todolist", value.getMessage());
                            todolistRepository.findByIdAndUser(todoUpdateRequest.getId(),savedUser)
                                    .ifPresent( todolist -> {
                                        assertEquals("Belajar Spring Security", todolist.getTodolist());
                                        assertFalse(todolist.getIsDone());
                                    });
                        }
                );
    }

    @Test
    void failedUpdateTodolistNotFound() throws Exception {
        todolistRepository.save(todolist());
        var token = createToken(userLogin());

        var todoUpdateRequest =  TodoUpdateRequest.builder()
                .id("sadjasd")
                .todo("Belajar Spring Security")
                .isDone(todolist().getIsDone())
                .build();

        mockMvc.perform(
                        put("/api/todolist/update/" + todoUpdateRequest.getId())
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(todoUpdateRequest))
                )
                .andExpectAll(
                        status().isNotFound())
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });
                            assertEquals("Todolist Not Found", value.getErrors());
                        }
                );
    }

    @Test
    void successDeleteTodolist() throws Exception{
        var save = todolistRepository.save(todolist());
        var token = createToken(userLogin());

        mockMvc.perform(
                        delete("/api/todolist/delete/" + save.getId())
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk())
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });

                            assertEquals("Success Delete Todolist", value.getMessage());
                            var todolist = todolistRepository.findByIdAndUser(save.getId(), savedUser);
                            assertTrue(todolist.isEmpty());
                        }
                );
    }

    @Test
    void failedDeleteTodolist() throws Exception {
        todolistRepository.save(todolist());

        var token = createToken(userLogin());

        mockMvc.perform(
                        delete("/api/todolist/delete/" + "asdasd")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound())
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });

                            assertEquals("Todolist Not Found", value.getErrors());
                        }
                );
    }

    @Test
    void successDeleteAllTodolist() throws Exception {

        var token = createToken(userLogin());

        mockMvc.perform(
                        delete("/api/todolist/delete-all")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk())
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });

                            assertEquals("Success Delete All Todolist", value.getMessage());

                            var allByUser = todolistRepository.findAllByUser(savedUser);
                            assertTrue(allByUser.isEmpty());

                        }
                );
    }

    @Test
    void successGetAllTodolist() throws Exception {
        var todolist1 = todolist();
        var todolist2 = todolist();
        todolist2.setTodolist("Belajar Spring Security");
        var todolist3 = todolist();
        todolist3.setTodolist("Belajar Spring WebMVC");
        var todolist4 = todolist();
        todolist4.setTodolist("Belajar Spring Boot");

        todolistRepository.saveAll(List.of(todolist1,todolist2,todolist3,todolist4));

        var token = createToken(userLogin());

        mockMvc.perform(
                        get("/api/todolist/get")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isOk())
                .andDo(
                        result -> {
                            WebResponse<List<TodoResponse>> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });
                            assertNotNull(value.getData());
                            assertEquals(4, value.getData().size());
                        }
                );
    }

    @Test
    void successGetTodolistOrderByDone() throws Exception {
        var todolist1 = todolist();
        var todolist2 = todolist();
        todolist2.setTodolist("Belajar Spring Security");
        todolist2.setIsDone(true);
        var todolist3 = todolist();
        todolist3.setTodolist("Belajar Spring WebMVC");
        todolist3.setIsDone(true);
        var todolist4 = todolist();
        todolist4.setTodolist("Belajar Spring Boot");

        todolistRepository.saveAll(List.of(todolist1,todolist2,todolist3,todolist4));

        var token = createToken(userLogin());

        mockMvc.perform(
                        get("/api/todolist/get-order-by-done")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                )
                .andExpectAll(
                        status().isOk())
                .andDo(
                        result -> {
                            WebResponse<List<TodoResponse>> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });

                            value.getData().forEach(System.out::println);
                            assertNotNull(value.getData());
                            assertEquals(4,value.getData().size());
                            var done = Sort.by(Sort.Order.desc("isDone"));
                            var allTodolist = todolistRepository
                                    .findAllByUser(savedUser,done);
                            assertTrue(allTodolist.get(0).getIsDone());
                            assertTrue(allTodolist.get(1).getIsDone());
                            assertFalse(allTodolist.get(2).getIsDone());
                            assertFalse(allTodolist.get(3).getIsDone());
                        }
                );
    }

    @Test
    void getTodolistOrderByName() throws Exception {
        var todolist1 = todolist();
        var todolist2 = todolist();
        todolist2.setTodolist("Belajar Spring Security");
        todolist2.setIsDone(true);
        var todolist3 = todolist();
        todolist3.setTodolist("Belajar Spring WebMVC");
        todolist3.setIsDone(true);
        var todolist4 = todolist();
        todolist4.setTodolist("Belajar Spring Boot");

        todolistRepository.saveAll(List.of(todolist1,todolist2,todolist3,todolist4));

        var token = createToken(userLogin());

        mockMvc.perform(
                        get("/api/todolist/get-order-by-name")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                )
                .andExpectAll(
                        status().isOk())
                .andDo(
                        result -> {
                            WebResponse<List<TodoResponse>> value = mapper.readValue(
                                    result.getResponse().getContentAsString(),
                                    new TypeReference<>() {
                                    });

                            value.getData().forEach(System.out::println);
                            assertNotNull(value.getData());
                            assertEquals(4,value.getData().size());
                            var name = Sort.by(Sort.Order.asc("todolist"));
                            var allTodolist = todolistRepository
                                    .findAllByUser(savedUser,name);
                            assertEquals("Belajar Spring", allTodolist.get(0).getTodolist());
                            assertEquals("Belajar Spring Boot", allTodolist.get(1).getTodolist());
                            assertEquals("Belajar Spring Security", allTodolist.get(2).getTodolist());
                            assertEquals("Belajar Spring WebMVC", allTodolist.get(3).getTodolist());
                        }
                );
    }
}