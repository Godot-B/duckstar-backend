package com.duckstar.service;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Season;
import com.duckstar.repository.SeasonRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
//@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class AnimeServiceTest {

    @Autowired AnimeService animeService;
    @Autowired
    private SeasonRepository seasonRepository;

    @Test
    @Transactional
    public void testCandidates() {
        Season season = seasonRepository.findById(2L).get();
        List<String> animeTitles = animeService.getAnimesForCandidate(
                true, season, LocalDateTime.of(2025, 9, 28, 22, 0))
                .stream().map(Anime::getTitleKor).toList();

        System.out.println("후보 수: " + animeTitles.size());
        for (String animeTitle : animeTitles) {
            System.out.println(animeTitle);
        }
    }
}
