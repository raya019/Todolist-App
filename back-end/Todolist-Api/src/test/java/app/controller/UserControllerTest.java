package app.controller;

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
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TodolistRepository todolistRepository;

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

    private WebResponse<Map<String,String>> createToken(Object request) throws Exception {
        return mapper.readValue(mockMvc.perform(
                        post("/api/auth/login")
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(request))
                )
                .andExpectAll(
                        status().isOk()
                ).andDo(
                        result -> {
                            WebResponse<Map<String,String>> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );
                        }
                ).andReturn().getResponse().getContentAsString(), new TypeReference<>() {
        });
    }

    private User user() {
        User userRequest = new User();
        userRequest.setName("Test");
        userRequest.setEmail("test@gmail.com");
        userRequest.setPassword(passwordEncoder.encode("password"));

        userRepository.save(userRequest);
        return userRequest;
    }

    private UserLogin userLogin() {
        return UserLogin.builder()
                .email("test@gmail.com")
                .password("password")
                .build();
    }

    @Test
    void getUserSuccess() throws Exception {
        var token = createToken(userLogin());

        mockMvc.perform(
                        get("/api/user/current")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(
                        result -> {
                            WebResponse<UserResponse> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );
                            assertNotNull(value.getData());
                            assertEquals(savedUser.getId(), value.getData().getId());
                            assertEquals(savedUser.getName(), value.getData().getName());
                            assertEquals(savedUser.getEmail(), value.getData().getEmail());
                        }
                );
    }

    @Test
    void failedGetUser() throws Exception {
        mockMvc.perform(
                        get("/api/user/current")
                )
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void successUpdateUser() throws Exception {
        var token = createToken(userLogin());

        var updateUserRequest =  UpdateUserRequest.builder()
                .name("Test Update")
                .build();

        mockMvc.perform(
                        patch("/api/user/current")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(updateUserRequest))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(
                        result -> {
                            WebResponse<UserResponse> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );

                            assertNotNull(value.getData());
                            userRepository.findByEmail(savedUser.getEmail()).ifPresent(user -> {
                                assertEquals(value.getData().getName(), user.getName());
                                assertEquals(value.getData().getId(),user.getId() );
                                assertEquals(value.getData().getEmail(),user.getEmail());
                            });

                        }
                );
    }

    @Test
    void failedUpdateUser() throws Exception {

        var updateUserRequest =  UpdateUserRequest.builder()
                .name("Test Update")
                .build();

        mockMvc.perform(
                        patch("/api/user/current")
                                .contentType("application/json")
                                .content(mapper.writeValueAsString(updateUserRequest))
                )
                .andExpectAll(
                        status().isUnauthorized()
                );
    }

    @Test
    void successChangePassword() throws Exception {
        var token = createToken(userLogin());

        var passwordRequest =  PasswordRequest.builder()
                .oldPassword("password")
                .newPassword("password123")
                .confirmPassword("password123")
                .build();

        mockMvc.perform(
                        post("/api/user/change-password")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(passwordRequest))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );

                            assertNotNull(value.getMessage());
                            assertEquals("Change Password Success", value.getMessage());

                            userRepository.findByEmail(savedUser.getEmail()).ifPresent(user -> {
                                assertTrue(passwordEncoder.matches(passwordRequest.getNewPassword(), user.getPassword()));
                            });

                        }
                );
    }

    @Test
    void failedChangePasswordOld() throws Exception {
        var token = createToken(userLogin());

        var passwordRequest =  PasswordRequest.builder()
                .oldPassword("password123")
                .newPassword("password")
                .confirmPassword("password")
                .build();

        mockMvc.perform(
                        post("/api/user/change-password")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(passwordRequest))
                )
                .andExpectAll(
                        status().isBadRequest()
                )
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );

                            assertNotNull(value.getErrors());
                            assertEquals("Old Password No Match", value.getErrors());

                        }
                );
    }

    @Test
    void failedChangePasswordConfirm() throws Exception {
        var token = createToken(userLogin());

        var passwordRequest =  PasswordRequest.builder()
                .oldPassword("password")
                .newPassword("password")
                .confirmPassword("passworddd")
                .build();

        mockMvc.perform(
                        post("/api/user/change-password")
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(passwordRequest))
                )
                .andExpectAll(
                        status().isBadRequest()
                )
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );

                            assertNotNull(value.getErrors());
                            assertEquals("New Password with Confirm Password No Match", value.getErrors());

                        }
                );
    }
}