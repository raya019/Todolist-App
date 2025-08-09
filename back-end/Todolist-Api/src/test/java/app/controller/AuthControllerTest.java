package app.controller;

import app.entity.RefreshToken;
import app.entity.User;
import app.model.RegisterRequest;
import app.model.ResponseToken;
import app.model.UserLogin;
import app.model.WebResponse;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TodolistRepository todolistRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        todolistRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
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

    private User user(){
        var user = new User();
        user.setName("Test12");
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

    @Test
    void failedRegister() throws Exception {
        userRepository.save(user());

        var request = RegisterRequest.builder()
                .name("Test12")
                .email("test@gmail.com")
                .password("rahasia123")
                .build();

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                )
                .andExpectAll(
                        status().isBadRequest()
                )
                .andDo(
                        value -> {
                            WebResponse<String> result = mapper.readValue(
                                    value.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );
                            assertNotNull(result.getErrors());
                            assertEquals("Email Already Exists",result.getErrors());
                        }
                );
    }

    @Test
    void successRegister() throws Exception {
        var request = RegisterRequest.builder()
                .name("test123")
                .email("test123@gmail.com")
                .password("rahasia123")
                .build();

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
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
                            assertEquals("Success Register", value.getMessage());
                        }
                );
    }

    @Test
    void failedLogin() throws Exception {
        userRepository.save(user());
        var request =  UserLogin.builder()
                .email("test@gmail.com")
                .password("rahasia")
                .build();

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
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
                            assertEquals("Incorrect username or password",value.getErrors());
                        }
                );
    }

    @Test
    void successLogin() throws Exception {
        userRepository.save(user());
        var request =  UserLogin.builder()
                .email("test@gmail.com")
                .password("password")
                .build();

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(
                        result -> {
                            WebResponse<ResponseToken> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );
                            assertNotNull(value.getData());
                            assertNotNull(result.getResponse().getCookies()[0].getValue());
                            assertEquals("Berhasil Login", value.getMessage());
                        }
                );

    }

    @Test
    void successRefreshToken() throws Exception {
        var savedUser = userRepository.save(user());

        var token = createToken(userLogin());
        var refreshToken = refreshTokenRepository.findByUserAndIsLogoutFalse(savedUser).orElseThrow();

        var cookie = new Cookie("refreshToken", refreshToken.getRefreshToken());

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))

                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(
                        result -> {
                            WebResponse<Map<String, String>> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );
                            assertNotNull(value.getData().get("accessToken"));
                            assertTrue(refreshTokenRepository.findByRefreshToken(refreshToken.getRefreshToken())
                                    .map(RefreshToken::getIsLogout).isPresent());
                        }
                );
    }

    @Test
    void failedRefreshTokenCookieNotFound() throws Exception {
        userRepository.save(user());
        var token = createToken(userLogin());

        var cookie = new Cookie("token", "token");

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(cookie)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                )
                .andExpectAll(
                        status().isUnauthorized()
                )
                .andDo(result ->{
                    WebResponse<String> value = mapper.readValue(
                            result.getResponse().getContentAsString(), new TypeReference<>() {}
                    );

                    assertNotNull(value.getErrors());
                    assertEquals("Unauthorized",value.getErrors());
                });
    }

    @Test
    void failedRefreshTokenHasLogout() throws Exception {
        var savedUser = userRepository.save(user());

        var token = createToken(userLogin());
        var refreshToken = refreshTokenRepository.findByUserAndIsLogoutFalse(savedUser).orElseThrow();

        refreshToken.setIsLogout(true);
        refreshTokenRepository.save(refreshToken);
        var cookie = new Cookie("refreshToken", refreshToken.getRefreshToken());

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))

                )
                .andExpectAll(
                        status().isUnauthorized()
                )
                .andDo(
                        result -> {
                            WebResponse<String> value = mapper.readValue(
                                    result.getResponse().getContentAsString(), new TypeReference<>() {
                                    }
                            );
                            assertNotNull(value.getErrors());
                            assertEquals("Unauthorized",value.getErrors());
                        }
                );
    }

    @Test
    void successLogout() throws Exception {
        var savedUser = user();
        userRepository.save(savedUser);
        var token = createToken(userLogin());
        var refreshToken = refreshTokenRepository.findByUserAndIsLogoutFalse(savedUser).orElseThrow();

        var cookie = new Cookie("refreshToken", refreshToken.getRefreshToken());

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(cookie)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                )
                .andExpectAll(
                        status().isOk()
                )
                .andDo(result ->{
                    WebResponse<String> value = mapper.readValue(
                            result.getResponse().getContentAsString(), new TypeReference<>() {}
                    );

                    assertNotNull(value.getMessage());
                    assertEquals("Logout Success",value.getMessage());
                    assertNull(SecurityContextHolder.getContext().getAuthentication());
                    refreshTokenRepository.findByRefreshToken(refreshToken.getRefreshToken()).ifPresent(token1 -> {
                        assertTrue(token1.getIsLogout());
                    });
                });
    }

    @Test
    void failedLogoutCookieNotFound() throws Exception {
        var savedUser = user();
        userRepository.save(savedUser);
        var token = createToken(userLogin());

        var cookie = new Cookie("token", "token");

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .cookie(cookie)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                )
                .andExpectAll(
                        status().isUnauthorized()
                )
                .andDo(result ->{
                    WebResponse<String> value = mapper.readValue(
                            result.getResponse().getContentAsString(), new TypeReference<>() {}
                    );

                    assertNotNull(value.getErrors());
                    assertEquals("Unauthorized",value.getErrors());
                });
    }

    @Test
    void failedLogoutRefreshTokenNotFound() throws Exception {
        var savedUser = user();
        userRepository.save(savedUser);
        var token = createToken(userLogin());
        var cookie = new Cookie("refreshToken", "token");

        mockMvc.perform(
                        post("/api/auth/logout")
                                .cookie(cookie)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer " + token.getData().get("accessToken"))
                )
                .andExpectAll(
                        status().isUnauthorized()
                )
                .andDo(result ->{
                    WebResponse<String> value = mapper.readValue(
                            result.getResponse().getContentAsString(), new TypeReference<>() {}
                    );

                    assertNotNull(value.getErrors());
                    assertEquals("Unauthorized",value.getErrors());
                });
    }
}