package com.duckstar.crawler.reader;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Character;
import com.duckstar.domain.Season;
import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Gender;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.mapping.AnimeCharacter;
import com.duckstar.domain.mapping.AnimeSeason;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.CharacterRepository;
import com.duckstar.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CsvImportService {
    private final CsvReader csvReader;
    private final AnimeRepository animeRepository;
    private final CharacterRepository characterRepository;

    @Transactional
    public void importAnimeCsv(String filePath) {
        List<Anime> animes = csvReader.readCsv(filePath, record -> {
            int minAge;
            try {
                minAge = Integer.parseInt(record.get("minAge"));
            } catch (NumberFormatException e) {
                minAge = 0;
            }

            return Anime.builder()
                    .medium(Medium.valueOf(record.get("medium")))
                    .status(AnimeStatus.valueOf(record.get("status")))
                    // totalEpisodes 나중에
                    .premiereDateTime(LocalDateTime.parse(record.get("premiereDateTime")))
                    .titleKor(record.get("titleKor"))
                    .titleOrigin(record.get("titleOrigin"))
                    // titleEng 나중에
                    .genre(record.get("genre"))
                    .dayOfWeek(DayOfWeekShort.valueOf(record.get("dayOfWeek")))
                    .airTime(record.get("airTime"))
                    .corp(record.get("corp"))
                    .director(record.get("director"))
                    .genre(record.get("genre"))
                    .author(record.get("author"))
                    .minAge(minAge)
                    // officialSite 나중에
                    .mainImageUrl(record.get("mainImageUrl"))
                    .mainThumbnailUrl(record.get("mainThumbnailUrl"))
                    .build();
        });

        animeRepository.saveAll(animes);
    }

    @Transactional
    public void importCharacterCsv(String filePath) {
        List<Character> characters = csvReader.readCsv(filePath, record -> Character.builder()
                    .nameKor(record.get("nameKor"))
                    .nameKanji(record.get("nameKanji"))
                    .nameEng(record.get("nameEng"))
                    .cv(record.get("cv"))
                    .mainThumbnailUrl(record.get("mainThumbnailUrl"))
                    .mainImageUrl(record.get("mainImageUrl"))
                    .build()
        );

        characterRepository.saveAll(characters);
    }
}
