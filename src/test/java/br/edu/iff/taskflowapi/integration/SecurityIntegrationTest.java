package br.edu.iff.taskflowapi.integration;

import br.edu.iff.taskflowapi.dto.LoginRequest;
import br.edu.iff.taskflowapi.dto.UserRequest;
import br.edu.iff.taskflowapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    private String email = "securityintegration@example.com";
    private String password = "password";
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
        // Signup
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Security Integration User");
        userRequest.setEmail(email);
        userRequest.setPassword(password);
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk());
        // Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andReturn();
        String jwt = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
        token = "Bearer " + jwt;
    }

    @AfterEach
    void tearDown() {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
    }

    // =============================
    // Security Integration Tests
    // =============================
    @Test
    void protectedEndpoint_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/task/all"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void protectedEndpoint_withValidToken_succeeds() throws Exception {
        mockMvc.perform(get("/api/task/all")
                .header("Authorization", token))
            .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withInvalidToken_fails() throws Exception {
        mockMvc.perform(get("/api/task/all")
                .header("Authorization", "Bearer invalid.jwt.token"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void publicEndpoints_areAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserRequest()))).andExpect(status().is4xxClientError()); // missing fields
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest()))).andExpect(status().is4xxClientError()); // missing fields
    }
} 