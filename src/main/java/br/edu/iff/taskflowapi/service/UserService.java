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
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        if (this.getByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("O email já está cadastrado.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        User user = this.getByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Senha inválida.");
        }

        return user;
    }
}
