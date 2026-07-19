package com.fintrack.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fintrack.auth.model.enums.Currency;
import com.fintrack.auth.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String id;
    private String fullName;
    private String username;
    private String email;
    private Set<Role> roles;
    private Currency currency;
    private LocalDateTime createdAt;
}

