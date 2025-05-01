package org.example.cloudstorage.service;

import org.example.cloudstorage.entity.User;

public interface AuthService {
    User putUserInContextWithAuthentication(User user);

    void putUserInContextWithoutAuthentication(User user);
}
