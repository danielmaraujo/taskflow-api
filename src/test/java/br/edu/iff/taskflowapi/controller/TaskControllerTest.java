package br.edu.iff.taskflowapi.controller;

import br.edu.iff.taskflowapi.model.Task;
import br.edu.iff.taskflowapi.security.JwtService;
import br.edu.iff.taskflowapi.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private TaskController taskController;

    private Task task;
    private String token;
    private String email;

    @BeforeEach
    void setUp() {
        token = "Bearer jwt";
        email = "test@example.com";
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("desc");
    }

    // =============================
    // getAll Tests
    // =============================
    @Test
    void getAll_returnsListOfTasks() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.getByEmail(email)).thenReturn(Collections.singletonList(task));
        ResponseEntity<List<Task>> response = taskController.getAll(token);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getTitle()).isEqualTo("Test Task");
        verify(taskService, times(1)).getByEmail(email);
    }

    // =============================
    // get Tests
    // =============================
    @Test
    void get_returnsTask() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.getById(task.getId(), email)).thenReturn(task);
        ResponseEntity<Task> response = taskController.get(token, task.getId());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(task.getId());
        verify(taskService, times(1)).getById(task.getId(), email);
    }

    // =============================
    // save Tests
    // =============================
    @Test
    void save_returnsCreatedTask() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.saveTask(task, email)).thenReturn(task);
        ResponseEntity<Task> response = taskController.save(token, task);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(task.getId());
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(taskService, times(1)).saveTask(task, email);
    }

    // =============================
    // update Tests
    // =============================
    @Test
    void update_returnsUpdatedTask() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.updateTask(any(Task.class), eq(email))).thenReturn(task);
        ResponseEntity<Task> response = taskController.update(token, task, task.getId());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(task.getId());
        verify(taskService, times(1)).updateTask(any(Task.class), eq(email));
    }

    // =============================
    // delete Tests
    // =============================
    @Test
    void delete_returnsNoContent() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        doNothing().when(taskService).deleteTask(task.getId(), email);
        ResponseEntity<Task> response = taskController.delete(token, task.getId());
        assertThat(response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()).isTrue();
        verify(taskService, times(1)).deleteTask(task.getId(), email);
    }

    // =============================
    // Exception propagation
    // =============================
    @Test
    void getAll_whenServiceThrows_propagatesException() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.getByEmail(email)).thenThrow(new RuntimeException("fail"));
        assertThatThrownBy(() -> taskController.getAll(token))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("fail");
    }

    @Test
    void get_whenServiceThrows_propagatesException() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.getById(task.getId(), email)).thenThrow(new RuntimeException("fail"));
        assertThatThrownBy(() -> taskController.get(token, task.getId()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("fail");
    }

    @Test
    void save_whenServiceThrows_propagatesException() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.saveTask(task, email)).thenThrow(new RuntimeException("fail"));
        assertThatThrownBy(() -> taskController.save(token, task))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("fail");
    }

    @Test
    void update_whenServiceThrows_propagatesException() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(taskService.updateTask(any(Task.class), eq(email))).thenThrow(new RuntimeException("fail"));
        assertThatThrownBy(() -> taskController.update(token, task, task.getId()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("fail");
    }

    @Test
    void delete_whenServiceThrows_propagatesException() {
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        doThrow(new RuntimeException("fail")).when(taskService).deleteTask(task.getId(), email);
        assertThatThrownBy(() -> taskController.delete(token, task.getId()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("fail");
    }
} 