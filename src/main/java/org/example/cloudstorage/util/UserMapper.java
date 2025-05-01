package org.example.cloudstorage.util;

import org.example.cloudstorage.dto.user.UserLoginRequest;
import org.example.cloudstorage.dto.user.UserRegisterRequest;
import org.example.cloudstorage.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(UserRegisterRequest registerRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(UserLoginRequest loginRequest);
}
