package org.example.cloudstorage.mapper;

import org.example.cloudstorage.dto.user.UserRegisterRequest;
import org.example.cloudstorage.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = PasswordEncodingMapper.class)
public interface UserMapper {
    @Mapping(source = "password", target = "password", qualifiedBy = EncodedMapping.class)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(UserRegisterRequest registerRequest);
}