//package com.duckstar.crawler.reader;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/import")
//public class CsvImportController {
//
//    private final CsvImportService csvImportService;
//
//    @PostMapping("/anime")
//    public String importAnime() {
//        csvImportService.importAnimeCsv("/Users/sungwoo/Projects/duckstar/scraper/data/animes.csv");
//        return "Anime CSV imported!";
//    }
//
//    @PostMapping("/characters")
//    public String importCharacters() {
//        csvImportService.importCharacterCsv("/Users/sungwoo/Projects/duckstar/scraper/data/characters.csv");
//        return "Character CSV imported!";
//    }
//}
