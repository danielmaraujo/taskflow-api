package br.edu.iff.taskflowapi.integration;

import br.edu.iff.taskflowapi.dto.UserRequest;
import br.edu.iff.taskflowapi.model.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    private String email = "registrationintegration@example.com";
    private String password = "password";

    @BeforeEach
    void setUp() {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
    }

    @AfterEach
    void tearDown() {
        userRepository.findByEmail(email).ifPresent(userRepository::delete);
    }

    // =============================
    // User Registration Integration Tests
    // =============================
    @Test
    void successful_registration_returnsUserWithoutPassword() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Registration Integration User");
        userRequest.setEmail(email);
        userRequest.setPassword(password);
        MvcResult result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andReturn();
        // Ensure user is in DB
        User user = userRepository.findByEmail(email).orElse(null);
        assertThat(user).isNotNull();
    }

    @Test
    void duplicate_email_registration_returns4xx() throws Exception {
        // First registration
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Registration Integration User");
        userRequest.setEmail(email);
        userRequest.setPassword(password);
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().isOk());
        // Duplicate registration
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void missing_fields_registration_returns4xx() throws Exception {
        UserRequest userRequest = new UserRequest();
        // No fields set
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
            .andExpect(status().is4xxClientError());
    }
} 