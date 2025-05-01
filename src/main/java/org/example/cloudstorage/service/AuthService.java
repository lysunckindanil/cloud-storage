package org.example.cloudstorage.service;

import org.example.cloudstorage.entity.User;

public interface AuthService {
    User authenticateUser(User user);

    void putUserInContextAsAuthenticated(User user);
}
