package com.habittracker.user.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.habittracker.user.api.validation.PasswordValidator;
import lombok.*;

/**
 * DTO for user registration.
 * Validates input; trims strings.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreateUserRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @PasswordValidator(message = "Password must be at least 8 characters with 1 uppercase and 1 digit")
    private String password;
}