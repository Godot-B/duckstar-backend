package com.duckstar.web.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EpisodeRequestDto {

    @NotNull
    @Min(1)
    @Max(100)
    Integer totalEpisodes;
}
