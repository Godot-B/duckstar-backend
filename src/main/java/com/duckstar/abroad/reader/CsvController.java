package com.duckstar.abroad.reader;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.duckstar.web.dto.admin.CsvRequestDto.*;

@RestController
@RequestMapping("/api/v1/csv")
@RequiredArgsConstructor
public class CsvController {

    private final CsvImportService csvImportService;

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
    public ResponseEntity<?> importNewSeason(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @ModelAttribute NewSeasonRequestDto request
    ) throws IOException {
        csvImportService.importNewSeason(year, quarter, request);
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }

    @PostMapping(value = "/import/{year}/{quarter}/{week}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importAbroad(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ModelAttribute AbroadRequestDto request
    ) throws IOException {
        csvImportService.importAbroad(year, quarter, week, request);
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }
}
