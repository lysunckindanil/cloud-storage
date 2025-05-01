package org.example.cloudstorage.service;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.config.security.CustomUserDetails;
import org.example.cloudstorage.entity.Role;
import org.example.cloudstorage.entity.User;
import org.example.cloudstorage.exception.UserWithThisNameAlreadyExistsException;
import org.example.cloudstorage.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new UserWithThisNameAlreadyExistsException("Username with username %s already exists"
                    .formatted(user.getUsername()));

        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}