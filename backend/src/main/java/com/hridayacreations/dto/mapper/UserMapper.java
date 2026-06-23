package com.hridayacreations.dto.mapper;

import com.hridayacreations.dto.response.UserResponse;
import com.hridayacreations.entity.Role;
import com.hridayacreations.entity.User;
import org.mapstruct.Mapper;

/**
 * Maps {@link User} entities to {@link UserResponse} DTOs. Roles are flattened to their names and
 * the password hash is never exposed.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    /** Element mapping used to translate a {@link Role} into its name for the roles set. */
    default String map(Role role) {
        return role.getName().name();
    }
}
