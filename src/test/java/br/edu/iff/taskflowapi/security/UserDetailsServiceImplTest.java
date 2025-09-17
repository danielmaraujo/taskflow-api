package br.edu.iff.taskflowapi.security;

import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPassword("secret");
    }

    // =============================
    // loadUserByUsername() Tests
    // =============================
    @Test
    void loadUserByUsername_whenUserExists_returnsUserDetailsImpl() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        assertThat(details).isInstanceOf(UserDetailsImpl.class);
        assertThat(details.getUsername()).isEqualTo(user.getEmail());
        assertThat(details.getPassword()).isEqualTo(user.getPassword());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(user.getEmail()))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User not found");
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }
} 