package br.edu.iff.taskflowapi.service;

import br.edu.iff.taskflowapi.model.User;
import br.edu.iff.taskflowapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> getByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }
        if (this.getByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("O email já está cadastrado.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser vazio.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser vazia.");
        }
        User user = this.getByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Senha inválida.");
        }

        return user;
    }
}
