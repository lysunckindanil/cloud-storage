package org.example.cloudstorage.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UserLoginRequest {
    @NotNull
    @Size(min = 3, max = 20)
    String username;

    @Size(min = 3, max = 20)
    @NotNull
    String password;
}
