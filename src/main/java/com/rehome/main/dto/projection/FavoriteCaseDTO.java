package com.rehome.main.dto.projection;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteCaseDTO {
    private Long caseId;
    private LocalDateTime favoritesDate;
}
