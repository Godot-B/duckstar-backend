package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.domain.enums.SeasonType;
import com.duckstar.service.SearchService;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.SearchResponseDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewListDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.duckstar.web.dto.SearchResponseDto.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {  // ⚠️ 분기 2개째 되면: 애니메이션 및 캐릭터 전체 검색 API 개발

    private final WeekService weekService;
    private final SearchService searchService;

    @GetMapping("/seasons")
    public ApiResponse<Map<Integer, List<SeasonType>>> getSeasons() {
        return ApiResponse.onSuccess(
                weekService.getSeasons());
    }

    @Operation(summary = "금주의 분류된 편성표 조회 API", description =
            """
                    분기 신작 애니는 많아도 100개 이하 -> 전체 조회
                    
                    -서버 역할
                    1. 정렬 - airTime asc
                    2. 그룹핑 - 총 9개 그룹: MON ~ SUN, SPECIAL(특별편성 및 극장판), NONE(TVA지만 미정 or 정보없음)
                    3. 상태 부여 - UPCOMING, NOW_SHOWING, COOLING, ENDED
                    
                    -클라이언트 역할
                    1. 그룹별 탭 전환
                    2. 기본 AnimeStatus 표시
                    3. 오늘과 같은 요일이고 && NOW_SHOWING && 아직 방영안한 애니들만: 방영까지 남은 시간 표시""")
    @GetMapping
    public ApiResponse<AnimePreviewListDto> getWeeklySchedule() {
        return ApiResponse.onSuccess(
                weekService.getWeeklySchedule());
    }

    @Operation(summary = "특정 시즌의 분류된 편성표 조회 API")
    @GetMapping("/{year}/{quarter}")
    public ApiResponse<AnimePreviewListDto> getScheduleByQuarter(
            @PathVariable Integer year, @PathVariable Integer quarter) {
        return ApiResponse.onSuccess(
                weekService.getScheduleByQuarterId(year, quarter));
    }

    @Operation(summary = "키워드를 통한 애니메이션 검색 API")
    @GetMapping("/animes")
    public ApiResponse<SearchResponseDto> searchAnimes(@RequestParam String query) {
        return ApiResponse.onSuccess(
                searchService.searchAnimes(query));
    }
}
