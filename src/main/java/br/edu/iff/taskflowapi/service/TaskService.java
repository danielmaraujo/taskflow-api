package br.edu.iff.taskflowapi.service;

import br.edu.iff.taskflowapi.model.Task;
import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public Task saveTask(Task task, String email) {
        User user = userService.getByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        task.setUser(user);
        return taskRepository.save(task);
    }

    public Task updateTask(Task task, String email) {
        Task taskDB = taskRepository.findById(task.getId())
            .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada."));

        if (!taskDB.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("Usuário não autorizado.");
        }

        task.setUser(taskDB.getUser());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id, String email) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tarefa não encontrada."));

        if (!task.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("Usuário não autorizado.");
        }

        taskRepository.delete(task);
    }

    public List<Task> getByUserId(Long userId){
        return taskRepository.findByUserId(userId);
    }
}
