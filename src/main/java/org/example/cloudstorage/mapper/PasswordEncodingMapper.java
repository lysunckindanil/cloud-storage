package org.example.cloudstorage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordEncodingMapper {

    private final PasswordEncoder passwordEncoder;

    @EncodedMapping
    public String encode(String value) {
        return passwordEncoder.encode(value);
    }
}
