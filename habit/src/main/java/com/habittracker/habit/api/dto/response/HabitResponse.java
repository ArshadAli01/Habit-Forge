package com.habittracker.habit.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.habittracker.habit.api.enums.Frequency;
import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO for habit details (excludes userId).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class HabitResponse {
    private Long id;
    private String name;
    private String description;
    private Frequency frequency;
    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
}