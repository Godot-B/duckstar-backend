package com.duckstar.web.dto.admin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CsvRequestDto {
    MultipartFile animeCsv;
    MultipartFile charactersCsv;
    MultipartFile animeCharactersCsv;
    MultipartFile episodesCsv;
}
