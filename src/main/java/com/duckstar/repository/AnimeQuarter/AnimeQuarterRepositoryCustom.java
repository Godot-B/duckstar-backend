package com.duckstar.repository.AnimeQuarter;

import com.duckstar.web.dto.AnimeResponseDto.QuarterDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.duckstar.web.dto.admin.ContentResponseDto.*;

public interface AnimeQuarterRepositoryCustom {
    List<AnimePreviewDto> getAnimePreviewsByQuarter(Long quarterId);
    List<QuarterDto> getQuarterDtosByAnimeId(Long animeId);
    Page<AdminAnimeDto> getAdminAnimeDtosByQuarterId(Long quarterId, Pageable pageable);
}
