package br.edu.iff.taskflowapi.service;

import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("plainPassword");
    }

    // ==================
    // getByEmail() Tests
    // ==================
    @Test
    void getByEmail_returnsUser() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Optional<User> result = userService.getByEmail(user.getEmail());
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void getByEmail_returnsEmptyWhenUserNotFound() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        Optional<User> result = userService.getByEmail(user.getEmail());
        assertThat(result).isNotPresent();
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void getByEmail_withEmptyEmail_throwsExceptionAndDoesNotCallRepository() {
        assertThatThrownBy(() -> userService.getByEmail(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ser vazio.");
        verifyNoInteractions(userRepository);
    }

    @Test
    void getByEmail_withNullEmail_throwsExceptionAndDoesNotCallRepository() {
        assertThatThrownBy(() -> userService.getByEmail(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ser vazio.");
        verifyNoInteractions(userRepository);
    }

    // ==================
    // saveUser() Tests
    // ==================
    @Test
    void saveUser_withUniqueEmail_savesUserWithEncodedPassword() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(user.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.saveUser(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void saveUser_withDuplicateEmail_throwsException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> userService.saveUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("O email já está cadastrado.");
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void saveUser_withEmptyEmail_throwsExceptionAndDoesNotCallRepository() {
        user.setEmail("");
        assertThatThrownBy(() -> userService.saveUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ser vazio.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void saveUser_withNullEmail_throwsExceptionAndDoesNotCallRepository() {
        user.setEmail(null);
        assertThatThrownBy(() -> userService.saveUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ser vazio.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void saveUser_withEmptyPassword_throwsExceptionAndDoesNotCallRepository() {
        user.setPassword("");
        assertThatThrownBy(() -> userService.saveUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha não pode ser vazia.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void saveUser_withNullPassword_throwsExceptionAndDoesNotCallRepository() {
        user.setPassword(null);
        assertThatThrownBy(() -> userService.saveUser(user))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha não pode ser vazia.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    // ==================
    // authenticateUser() Tests
    // ==================
    @Test
    void authenticateUser_withValidCredentials_returnsUser() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", user.getPassword())).thenReturn(true);
        Optional<User> found = userService.getByEmail(user.getEmail());
        User authenticated = userService.authenticateUser(user.getEmail(), "plainPassword");
        assertThat(authenticated).isEqualTo(user);
        verify(userRepository, times(2)).findByEmail(user.getEmail());
        verify(passwordEncoder, times(1)).matches("plainPassword", user.getPassword());
    }

    @Test
    void authenticateUser_withNonexistentUser_throwsException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.authenticateUser(user.getEmail(), "plainPassword"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Usuário não encontrado.");
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticateUser_withInvalidPassword_throwsException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);
        assertThatThrownBy(() -> userService.authenticateUser(user.getEmail(), "wrongPassword"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha inválida.");
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(passwordEncoder, times(1)).matches("wrongPassword", user.getPassword());
    }

    @Test
    void authenticateUser_withEmptyEmail_throwsExceptionAndDoesNotCallRepository() {
        assertThatThrownBy(() -> userService.authenticateUser("", "any"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ser vazio.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void authenticateUser_withNullEmail_throwsExceptionAndDoesNotCallRepository() {
        assertThatThrownBy(() -> userService.authenticateUser(null, "any"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email não pode ser vazio.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void authenticateUser_withEmptyPassword_throwsExceptionAndDoesNotCallRepository() {
        assertThatThrownBy(() -> userService.authenticateUser(user.getEmail(), ""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha não pode ser vazia.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void authenticateUser_withNullPassword_throwsExceptionAndDoesNotCallRepository() {
        assertThatThrownBy(() -> userService.authenticateUser(user.getEmail(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Senha não pode ser vazia.");
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }
} 