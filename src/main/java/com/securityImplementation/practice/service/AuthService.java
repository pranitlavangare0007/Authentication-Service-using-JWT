package com.securityImplementation.practice.service;

import com.securityImplementation.practice.entity.AppUser;
import com.securityImplementation.practice.repository.AppUserRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AppUserRepo userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Username already taken");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
