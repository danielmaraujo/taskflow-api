package br.edu.iff.taskflowapi.service;

import br.edu.iff.taskflowapi.dto.TaskRequest;
import br.edu.iff.taskflowapi.model.Status;
import br.edu.iff.taskflowapi.model.Task;
import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public Task saveTask(TaskRequest taskRequest, String email) {
        User user = userService.getByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não encontrado."));

        Task task = new Task();
        task.setTitle(taskRequest.getTitle());
        task.setDescription(taskRequest.getDescription());
        task.setLimitDate(LocalDate.parse(taskRequest.getLimitDate()));
        task.setCreationDate(LocalDate.now());
        task.setStatus(Status.OPEN);
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
