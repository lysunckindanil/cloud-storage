package org.example.cloudstorage.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UserLoginRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    String username;

    @Size(min = 3, max = 20)
    @NotBlank
    String password;
}
