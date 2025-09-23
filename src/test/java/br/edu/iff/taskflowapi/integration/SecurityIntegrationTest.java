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
        // signup
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Security Integration User");
        userRequest.setEmail(email);
        userRequest.setPassword(password);
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk());
        // login
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
    void publicEndpoint_signup_allowsAccess() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Public Test User");
        userRequest.setEmail("public@example.com");
        userRequest.setPassword("password");
        
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk());
            
        userRepository.findByEmail("public@example.com").ifPresent(userRepository::delete);
    }

    @Test
    void publicEndpoint_login_allowsAccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk());
    }

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
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withExpiredToken_fails() throws Exception {
        String expiredToken = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJzcHJpbmctc2VjdXJpdHktand0Iiwic3ViIjoiZGFuaWVsYXJhdWpvQGdtYWlsLmNvbSIsImV4cCI6MTc1ODY1NjYzNywiaWF0IjoxNzU4NjUzMDM3fQ.K2s6eiyIZ0yt9eSSl9G0V9zVocxkTpP7KSI5FZtdfvCZqMV3kPirYthsRfaokd5_f3JQEPy-guDTNodgmYI43IibWIxWjco_iZG1bgV-1iVIyZ9I1cfozsRoTIB-eA2CrOO6ADd1MEVtqZi1EBUvhQ7oU5kWQIQHAPXrw6a0mJAmX7tjk06iVXQmwYJTVyx3thIbTrG4WRJOt8OFJQkuaHmbz4RU0lQBDn-AAP2Z0zPQV7rmUjHfIwkBkRtu9gdE-vm_WJ3pKxzoF2Bn1NSJdWErW_6ap_Q3K5EV1tr9Qk_UkEZa43XBGMlKsKgVgDUn_UNUXJ60eKCnMo7pz04z8g";
        
        mockMvc.perform(get("/api/task/all")
                .header("Authorization", expiredToken))
            .andExpect(status().isUnauthorized());
    }
}