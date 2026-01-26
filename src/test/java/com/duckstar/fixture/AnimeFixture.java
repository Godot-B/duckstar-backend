package com.duckstar.fixture;

import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.web.dto.admin.AnimeRequestDto.PostRequestDto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class AnimeFixture {

    public static PostRequestDto.PostRequestDtoBuilder tvaRequestBuilder() {
        return PostRequestDto.builder()
                .titleKor("TVA_테스트")
                .titleEng("test")
                .titleOrigin("テスト")
                .medium(Medium.TVA)
                .premiereDateTime(LocalDateTime
                        .of(2026, 1, 5,
                                20, 0))
                .dayOfWeek(DayOfWeekShort.MON)
                .airTime(LocalTime.of(20, 0))
                .totalEpisodes(null)
                .corp("corp")
                .director("director")
                .genre("genre")
                .author("author")
                .minAge(0)
                .officialSiteString("{\"X\": \"...\", \"OTHERS\": \"...\"}")
                .synopsis("synopsis")
                .mainImage(null)
                .ottDtos(null);
    }
}
