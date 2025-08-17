package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.service.AnimeService;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.SearchResponseDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewListDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Validated
public class SearchController {  // ⚠️ 분기 2개째 되면: 애니메이션 및 캐릭터 전체 검색 API 개발

    private final WeekService weekService;
    private final AnimeService animeService;

    @Operation(summary = "분류된 편성표 조회 API", description =
            """
                    분기 신작 애니는 많아도 100개 이하 -> 전체 조회
                    
                    -서버 역할
                    1. 정렬 - airTime asc
                    2. 그룹핑 - 총 9개 그룹: MON ~ SUN, SPECIAL(특별편성 및 극장판), NONE(TVA지만 미정 or 정보없음)
                    3. 상태 부여 - UPCOMING, NOW_SHOWING, COOLING, ENDED
                    
                    -클라이언트 역할
                    1. 그룹별 탭 전환
                    2. 기본 상태 표시 <- NOW_SHOWING 경우만 방영까지 남은 시간 표시
                    3. 검색 - 애니메이션 제목 쿼리, 애니메이션 OTT 태그 필터 (메모리 상에서)""")
    @GetMapping("/{year}/{quarter}")
    public ApiResponse<AnimePreviewListDto> getSchedule(@PathVariable Integer year, @PathVariable Integer quarter) {
        Long quarterId = weekService.getQuarterIdByYQ(year, quarter);

        return ApiResponse.onSuccess(
                animeService.getScheduleByQuarterId(quarterId));
    }

    // 캐릭터 검색 결과 반환 API


    // ⚠️ 분기 2개째 되면: 애니메이션 및 캐릭터 전체 검색 API 개발
}
