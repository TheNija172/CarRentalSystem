package org.example.carrentalsystem.mapper;

import org.example.carrentalsystem.dto.user.UserCreateRequest;
import org.example.carrentalsystem.dto.user.UserResponse;
import org.example.carrentalsystem.dto.user.UserUpdateRequest;
import org.example.carrentalsystem.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface  UserMapper {

    @Mapping(source = "role.name", target = "role")
    UserResponse toResponse(UserEntity userEntity);

    @Mapping(target = "role", ignore = true)
    UserEntity toEntity(UserCreateRequest request);

    @Mapping(target = "role", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget UserEntity userEntity);

}
