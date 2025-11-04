package com.habittracker.habit.api.dto.response;

import lombok.*;
import java.util.List;

/**
 * Paginated response for habits.
 * Includes metadata for frontend.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedHabitResponse {
    private List<HabitResponse> habits;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static PagedHabitResponse of(List<HabitResponse> habits, int page, int size, long total) {
        int totalPages = (int) Math.ceil((double) total / size);
        return PagedHabitResponse.builder()
                .habits(habits)
                .page(page)
                .size(size)
                .totalElements(total)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
}