package org.example.cloudstorage.util;

import org.example.cloudstorage.dto.user.UserLoginRequest;
import org.example.cloudstorage.dto.user.UserRegisterRequest;
import org.example.cloudstorage.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(UserRegisterRequest registerRequest);

    User toUser(UserLoginRequest loginRequest);
}
