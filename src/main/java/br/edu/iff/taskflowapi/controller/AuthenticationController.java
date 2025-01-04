package br.edu.iff.taskflowapi.controller;

import br.edu.iff.taskflowapi.dto.LoginRequest;
import br.edu.iff.taskflowapi.dto.LoginResponse;
import br.edu.iff.taskflowapi.dto.UserRequest;
import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.security.JwtService;
import br.edu.iff.taskflowapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthenticationController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> cadastrarUsuario(@Valid @RequestBody UserRequest requestUser) {
        User user = new User();
        user.setName(requestUser.getName());
        user.setEmail(requestUser.getEmail());
        user.setPassword(requestUser.getPassword());

        User createdUser = userService.saveUser(user);
        createdUser.setPassword(null);

        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtService.generateToken(user));

        return ResponseEntity.ok(loginResponse);
    }
}
