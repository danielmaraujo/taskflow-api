package br.edu.iff.taskflowapi.integration;

import br.edu.iff.taskflowapi.dto.LoginRequest;
import br.edu.iff.taskflowapi.dto.TaskRequest;
import br.edu.iff.taskflowapi.dto.UserRequest;
import br.edu.iff.taskflowapi.model.Status;
import br.edu.iff.taskflowapi.model.Task;
import br.edu.iff.taskflowapi.repository.TaskRepository;
import br.edu.iff.taskflowapi.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskRepository taskRepository;

    private String email = "taskintegration@example.com";
    private String password = "password";
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        // clean up user and tasks
        userRepository.findByEmail(email).ifPresent(user -> {
            taskRepository.deleteAll(taskRepository.findByUserEmail(email));
            userRepository.delete(user);
        });
        // signup
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Task Integration User");
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
        userRepository.findByEmail(email).ifPresent(user -> {
            taskRepository.deleteAll(taskRepository.findByUserEmail(email));
            userRepository.delete(user);
        });
    }

    // =============================
    // Task CRUD Integration Tests
    // =============================
    @Test
    void create_getAll_getById_update_delete_flow() throws Exception {
        // create
        TaskRequest task = new TaskRequest();
        task.setTitle("Integration Task");
        task.setDescription("desc");
        task.setLimitDate("2025-09-22");
        MvcResult createResult = mockMvc.perform(post("/api/task")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
            .andExpect(status().isCreated())
            .andReturn();
        Task createdTask = objectMapper.readValue(createResult.getResponse().getContentAsString(), Task.class);
        assertThat(createdTask.getId()).isNotNull();
        assertThat(createdTask.getTitle()).isEqualTo("Integration Task");
        assertThat(createdTask.getStatus()).isEqualTo(Status.OPEN);
        assertThat(createdTask.getCreationDate()).isEqualTo(LocalDate.now());
        assertThat(createdTask.getLimitDate()).isEqualTo(LocalDate.parse("2025-09-22"));
        assertThat(createdTask.getDescription()).isEqualTo("desc");

        // get all
        MvcResult allResult = mockMvc.perform(get("/api/task/all")
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andReturn();
        List<Task> tasks = objectMapper.readValue(allResult.getResponse().getContentAsString(), new TypeReference<List<Task>>(){});
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.stream().anyMatch(t -> t.getId().equals(createdTask.getId()))).isTrue();

        // get by id
        MvcResult getResult = mockMvc.perform(get("/api/task/" + createdTask.getId())
                .header("Authorization", token))
            .andExpect(status().isOk())
            .andReturn();
        Task fetchedTask = objectMapper.readValue(getResult.getResponse().getContentAsString(), Task.class);
        assertThat(fetchedTask.getId()).isEqualTo(createdTask.getId());

        // update
        createdTask.setTitle("Updated Title");
        MvcResult updateResult = mockMvc.perform(put("/api/task/" + createdTask.getId())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdTask)))
            .andExpect(status().isOk())
            .andReturn();
        Task updatedTask = objectMapper.readValue(updateResult.getResponse().getContentAsString(), Task.class);
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");

        // delete
        mockMvc.perform(delete("/api/task/" + createdTask.getId())
                .header("Authorization", token))
            .andExpect(status().isNoContent());
        assertThat(taskRepository.findById(createdTask.getId())).isEmpty();
    }

    @Test
    void unauthorized_access_returns4xx() throws Exception {
        // try to get all tasks without token
        mockMvc.perform(get("/api/task/all"))
            .andExpect(status().is4xxClientError());
        // try to create task without token
        Task task = new Task();
        task.setTitle("No Auth");
        mockMvc.perform(post("/api/task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task)))
            .andExpect(status().is4xxClientError());
    }
} 