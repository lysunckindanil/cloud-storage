package org.example.cloudstorage.service;

import org.example.cloudstorage.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);

    User register(User user);
}
