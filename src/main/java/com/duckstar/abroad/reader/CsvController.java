package com.duckstar.abroad.reader;

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

    @PostMapping(value = "/import/{year}/{quarter}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importNewSeason(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @ModelAttribute NewSeasonRequestDto request
    ) throws IOException {
        csvImportService.importNewSeason(year, quarter, request);
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }

    @PostMapping(value = "/import/{weekId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importAbroad(
            @PathVariable Long weekId,
            @ModelAttribute AbroadRequestDto request
    ) throws IOException {
        csvImportService.importAbroad(weekId, request);
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }
}
