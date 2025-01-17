package br.edu.iff.taskflowapi.service;

import br.edu.iff.taskflowapi.model.Task;
import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado."));

        task.setUser(user);
        return taskRepository.save(task);
    }

    public Task updateTask(Task task, String email) {
        Task taskDB = taskRepository.findById(task.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada."));

        if (!taskDB.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não autorizado.");
        }

        taskDB.setDescription(task.getDescription());
        taskDB.setStatus(task.getStatus());
        taskDB.setTitle(task.getTitle());
        taskDB.setLimitDate(task.getLimitDate());
        return taskRepository.save(taskDB);
    }

    public void deleteTask(Long id, String email) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada."));

        if (!task.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não autorizado.");
        }

        taskRepository.delete(task);
    }

    public List<Task> getByEmail(String email){
        return taskRepository.findByUserEmail(email);
    }

    public Task getById(Long id, String email) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada."));

        if (!task.getUser().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não autorizado.");
        }

        return task;
    }
}
