package br.edu.iff.taskflowapi.controller;

import br.edu.iff.taskflowapi.dto.TaskResponse;
import br.edu.iff.taskflowapi.security.JwtService;
import br.edu.iff.taskflowapi.service.TaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
    @ResponseBody
    public String getAll(@RequestHeader("Authorization") String authorizationToken){
        List<TaskResponse> tasks = taskService.getByEmail(jwtService.getEmailFromToken(authorizationToken));
        //wip

    }
}
