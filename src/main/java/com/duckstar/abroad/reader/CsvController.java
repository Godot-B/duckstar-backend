package com.duckstar.abroad.reader;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Quarter;
import com.duckstar.service.QuarterService;
import com.duckstar.service.WeekService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

import static com.duckstar.web.dto.admin.CsvRequestDto.*;

@RestController
@RequestMapping("/api/v1/csv")
@RequiredArgsConstructor
public class CsvController {

    private final CsvImportService csvImportService;
    private final WeekService weekService;
    private final QuarterService quarterService;

    @Operation(summary = "어워드 후보 csv를 서버에 변환 및 업로드")
    @PostMapping(value = "/import/surveys/{surveyId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importYearCandidates(
            @PathVariable Long surveyId,
            @ModelAttribute CandidatesCsvRequest request
    ) throws IOException {
        csvImportService.importCandidates(surveyId, request.getCandidatesCsv());
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }

    @Operation(summary = "새로운 분기 정보 csv를 서버에 변환 및 업로드")
    @PostMapping(value = "/import/{year}/{quarter}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importNewQuarter(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @ModelAttribute NewQuarterRequestDto request
    ) throws IOException {
        Quarter savedQuarter = quarterService.getOrCreateQuarter(year, quarter);

        Map<Integer, Long> animeIdMap = csvImportService.importAnimes(savedQuarter, request.getAnimeCsv());
        Map<Integer, Long> characterIdMap = csvImportService.importCharacters(request.getCharactersCsv());

        Map<Long, Anime> animeMap = csvImportService.importAnimeCharacters(
                request.getAnimeCharactersCsv(), animeIdMap, characterIdMap);
        csvImportService.importEpisodes(request.getEpisodesCsv(), animeMap, animeIdMap);
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }

    @PostMapping(value = "/import/{year}/{quarter}/{week}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importAbroad(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ModelAttribute AbroadRequestDto request
    ) throws IOException {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        csvImportService.importAnimeCorner(weekId, request.getAnimeCornerCsv());
        csvImportService.importAnilab(weekId, request.getAnilabCsv());
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }
}
