package org.example.cloudstorage.repo;

import org.example.cloudstorage.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}