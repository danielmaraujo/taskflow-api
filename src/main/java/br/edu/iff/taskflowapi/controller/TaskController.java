package br.edu.iff.taskflowapi.controller;

import br.edu.iff.taskflowapi.model.Task;
import br.edu.iff.taskflowapi.security.JwtService;
import br.edu.iff.taskflowapi.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    private final TaskService taskService;
    private final JwtService jwtService;

    public TaskController(TaskService taskService, JwtService jwtService) {
        this.taskService = taskService;
        this.jwtService = jwtService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Task>> getAll(@RequestHeader("Authorization") String authorizationToken){
        return ResponseEntity.ok(taskService.getByEmail(jwtService.getEmailFromToken(authorizationToken)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> get(@RequestHeader("Authorization") String authorizationToken, @PathVariable Long id){
        return ResponseEntity.ok(taskService.getById(id, jwtService.getEmailFromToken(authorizationToken)));
    }

    @PostMapping
    public ResponseEntity<Task> save(@RequestHeader("Authorization") String authorizationToken, @RequestBody Task task){
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(task.getId())
            .toUri();

        return ResponseEntity.created(location).body(taskService.saveTask(task, jwtService.getEmailFromToken(authorizationToken)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@RequestHeader("Authorization") String authorizationToken, @RequestBody Task task, @PathVariable("id") Long id){
        task.setId(id);
        return ResponseEntity.ok().body(taskService.updateTask(task, jwtService.getEmailFromToken(authorizationToken)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Task> delete(@RequestHeader("Authorization") String authorizationToken, @PathVariable("id") Long id){
        taskService.deleteTask(id, jwtService.getEmailFromToken(authorizationToken));
        return ResponseEntity.noContent().build();
    }
}
