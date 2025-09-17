package br.edu.iff.taskflowapi.controller;

import br.edu.iff.taskflowapi.dto.LoginRequest;
import br.edu.iff.taskflowapi.dto.LoginResponse;
import br.edu.iff.taskflowapi.dto.UserRequest;
import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.security.JwtService;
import br.edu.iff.taskflowapi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthenticationController authenticationController;

    private UserRequest userRequest;
    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userRequest = new UserRequest();
        userRequest.setName("Test User");
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("secret");

        user = new User();
        user.setId(1L);
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(userRequest.getPassword());

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("secret");
    }

    // =============================
    // signup (cadastrarUsuario) Tests
    // =============================
    @Test
    void cadastrarUsuario_returnsCreatedUserWithPasswordNull() {
        User created = new User();
        created.setId(1L);
        created.setName(userRequest.getName());
        created.setEmail(userRequest.getEmail());
        created.setPassword("encoded");
        when(userService.saveUser(any(User.class))).thenReturn(created);

        ResponseEntity<User> response = authenticationController.cadastrarUsuario(userRequest);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo(userRequest.getName());
        assertThat(response.getBody().getEmail()).isEqualTo(userRequest.getEmail());
        assertThat(response.getBody().getPassword()).isNull();
        verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    void cadastrarUsuario_whenServiceThrows_propagatesException() {
        when(userService.saveUser(any(User.class))).thenThrow(new IllegalArgumentException("fail"));
        assertThatThrownBy(() -> authenticationController.cadastrarUsuario(userRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("fail");
        verify(userService, times(1)).saveUser(any(User.class));
    }

    // =============================
    // login Tests
    // =============================
    @Test
    void login_returnsToken() {
        when(userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword())).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        ResponseEntity<LoginResponse> response = authenticationController.login(loginRequest);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("jwt-token");
        verify(userService, times(1)).authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void login_whenServiceThrows_propagatesException() {
        when(userService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword())).thenThrow(new IllegalArgumentException("fail"));
        assertThatThrownBy(() -> authenticationController.login(loginRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("fail");
        verify(userService, times(1)).authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
    }
} 