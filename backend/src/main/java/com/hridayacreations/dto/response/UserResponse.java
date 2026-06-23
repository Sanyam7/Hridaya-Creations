package com.hridayacreations.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

/**
 * Public representation of a user account. Never exposes the password hash.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserResponse", description = "User account details")
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String mobileNumber;
    private Set<String> roles;
    private boolean enabled;
    private Instant createdAt;
}
