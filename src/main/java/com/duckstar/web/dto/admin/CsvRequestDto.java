package com.duckstar.web.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class CsvRequestDto {

    @Getter
    @Setter
    public static class NewSeasonRequestDto {
        @Schema(type = "string", format = "binary")
        MultipartFile animeCsv;

        @Schema(type = "string", format = "binary")
        MultipartFile charactersCsv;

        @Schema(type = "string", format = "binary")
        MultipartFile animeCharactersCsv;

        @Schema(type = "string", format = "binary")
        MultipartFile episodesCsv;
    }

    @Getter
    @Setter
    public static class AbroadRequestDto {
        @Schema(type = "string", format = "binary")
        MultipartFile animeTrendingCsv;

        @Schema(type = "string", format = "binary")
        MultipartFile anilabCsv;
    }
}
