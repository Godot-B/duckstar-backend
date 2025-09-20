package com.duckstar.crawler.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/import")
public class CsvImportController {

    private final CsvImportService csvImportService;

    @PostMapping("/new-season")
    public ResponseEntity<?> importData(
            @RequestPart("animes") MultipartFile animesFile,
            @RequestPart("characters") MultipartFile charactersFile,
            @RequestPart("animeCharacters") MultipartFile animeCharactersFile,
            @RequestPart("episodes") MultipartFile episodesFile
    ) {
//        csvImportService.importAll(animesFile, charactersFile, animeCharactersFile, episodesFile);
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }
}
