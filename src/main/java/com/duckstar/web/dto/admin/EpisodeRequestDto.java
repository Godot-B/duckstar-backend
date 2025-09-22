package com.duckstar.web.dto.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class EpisodeRequestDto {

    @NotNull
    @Size(min = 1, max = 100)
    Integer totalEpisodes;
}
