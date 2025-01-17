package br.edu.iff.taskflowapi.service;

import br.edu.iff.taskflowapi.model.Status;
import br.edu.iff.taskflowapi.model.Task;
import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");

        task = new Task();
        task.setId(10L);
        task.setTitle("Sample Task");
        task.setDescription("Sample Description");
        task.setStatus(Status.OPEN);
        task.setLimitDate(LocalDate.now().plusDays(5));
    }

    // ==================
    // saveTask() Tests
    // ==================
    @Test
    void saveTask_withExistingUser_savesTask() {
        // given
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Task savedTask = taskService.saveTask(task, user.getEmail());

        // then
        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getUser()).isEqualTo(user);
        verify(userService, times(1)).getByEmail(user.getEmail());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void saveTask_withNonExistingUser_throwsResponseStatusException() {
        // given
        when(userService.getByEmail(user.getEmail())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> taskService.saveTask(task, user.getEmail()))
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(ex.getReason()).isEqualTo("Usuário não encontrado.");
            });
    }

    // ==================
    // updateTask() Tests
    // ==================
    @Test
    void updateTask_withExistingTaskAndMatchingUser_updatesAndSavesTask() {
        // given
        Task existingTask = new Task();
        existingTask.setId(task.getId());
        existingTask.setUser(user);  // current user matches
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setStatus(Status.OPEN);
        existingTask.setLimitDate(LocalDate.now().plusDays(10));

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Task updatedTask = taskService.updateTask(task, user.getEmail());

        // then
        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getTitle()).isEqualTo("Sample Task");
        assertThat(updatedTask.getDescription()).isEqualTo("Sample Description");
        assertThat(updatedTask.getLimitDate()).isEqualTo(task.getLimitDate());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void updateTask_withNonExistingTask_throwsNotFound() {
        // given
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> taskService.updateTask(task, user.getEmail()))
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(ex.getReason()).isEqualTo("Tarefa não encontrada.");
            });
    }

    @Test
    void updateTask_withMismatchedUserEmail_throwsForbidden() {
        // given
        Task existingTask = new Task();
        existingTask.setId(task.getId());

        User anotherUser = new User();
        anotherUser.setEmail("other@example.com");
        existingTask.setUser(anotherUser);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(existingTask));

        // when / then
        assertThatThrownBy(() -> taskService.updateTask(task, user.getEmail()))
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                assertThat(ex.getReason()).isEqualTo("Usuário não autorizado.");
            });
    }

    // ==================
    // deleteTask() Tests
    // ==================
    @Test
    void deleteTask_withExistingTaskAndMatchingUser_deletesTask() {
        // given
        Task existingTask = new Task();
        existingTask.setId(20L);
        existingTask.setUser(user);

        when(taskRepository.findById(20L)).thenReturn(Optional.of(existingTask));

        // when
        taskService.deleteTask(20L, user.getEmail());

        // then
        verify(taskRepository, times(1)).delete(existingTask);
    }

    @Test
    void deleteTask_withNonExistingTask_throwsNotFound() {
        // given
        when(taskRepository.findById(20L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> taskService.deleteTask(20L, user.getEmail()))
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(ex.getReason()).isEqualTo("Tarefa não encontrada.");
            });
    }

    @Test
    void deleteTask_withMismatchedUser_throwsForbidden() {
        // given
        Task existingTask = new Task();
        existingTask.setId(20L);

        User anotherUser = new User();
        anotherUser.setEmail("someoneelse@example.com");
        existingTask.setUser(anotherUser);

        when(taskRepository.findById(20L)).thenReturn(Optional.of(existingTask));

        // when / then
        assertThatThrownBy(() -> taskService.deleteTask(20L, user.getEmail()))
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                assertThat(ex.getReason()).isEqualTo("Usuário não autorizado.");
            });
    }

    // ==================
    // getByEmail() Tests
    // ==================
    @Test
    void getByEmail_returnsListOfTasks() {
        // given
        List<Task> tasks = new ArrayList<>();
        tasks.add(task);
        when(taskRepository.findByUserEmail(user.getEmail())).thenReturn(tasks);

        // when
        List<Task> result = taskService.getByEmail(user.getEmail());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Sample Task");
        verify(taskRepository, times(1)).findByUserEmail(user.getEmail());
    }

    // ==================
    // getById() Tests
    // ==================
    @Test
    void getById_withValidTaskAndMatchingUser_returnsTask() {
        // given
        task.setUser(user);
        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        // when
        Task foundTask = taskService.getById(task.getId(), user.getEmail());

        // then
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getId()).isEqualTo(task.getId());
        assertThat(foundTask.getUser().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getById_withNonExistingTask_throwsNotFound() {
        // given
        when(taskRepository.findById(task.getId())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> taskService.getById(task.getId(), user.getEmail()))
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                assertThat(ex.getReason()).isEqualTo("Tarefa não encontrada.");
            });
    }

    @Test
    void getById_withMismatchedUser_throwsForbidden() {
        // given
        Task existingTask = new Task();
        existingTask.setId(task.getId());

        User anotherUser = new User();
        anotherUser.setEmail("someoneelse@example.com");
        existingTask.setUser(anotherUser);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(existingTask));

        // when / then
        assertThatThrownBy(() -> taskService.getById(task.getId(), user.getEmail()))
            .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                assertThat(ex.getReason()).isEqualTo("Usuário não autorizado.");
            });
    }
}

