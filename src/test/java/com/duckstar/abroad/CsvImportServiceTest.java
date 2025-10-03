package com.duckstar.abroad;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Season;
import com.duckstar.domain.mapping.AnimeSeason;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.SeasonRepository;
import com.duckstar.s3.S3Uploader;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class CsvImportServiceTest {

    @Autowired
    AnimeRepository animeRepository;
    @Autowired
    private SeasonRepository seasonRepository;
    @Autowired
    private AnimeSeasonRepository animeSeasonRepository;
    @Autowired
    private S3Uploader s3Uploader;

    @Test
    @Transactional
    @Rollback(false)
    public void seasonTag() throws Exception {
        Season season = seasonRepository.findById(2L).get();
        List<Anime> animes = animeRepository.findAllByIdGreaterThanEqual(66L);

        for (Anime anime : animes) {
            animeSeasonRepository.save(AnimeSeason.create(anime, season));
        }
    }

    @Test
    @Transactional
    @Rollback(false)
    public void uploadThumbnails() throws Exception {
        List<Anime> animes = animeSeasonRepository.findAllBySeason_Id(2L)
                .stream().map(AnimeSeason::getAnime)
                .toList();

        for (Anime anime : animes) {
            Long animeId = anime.getId();
            String mainImageUrl = anime.getMainImageUrl();

            if (mainImageUrl == null || mainImageUrl.isBlank()) {
                System.out.println("⚠️ 이미지 없음, id: " + animeId);
                continue;
            }

            try {
                File tempDir = Files.createTempDirectory("anime").toFile();

                // 1. 원본 다운로드 (이미 S3에 있으니까 URL에서 가져옴)
                File original = new File(tempDir, "original.webp");
                try (InputStream in = new URL(mainImageUrl).openStream()) {
                    Files.copy(in, original.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                // 2. 썸네일 다시 생성 (315x475 고정 비율로 리사이즈)
                ImmutableImage img = ImmutableImage.loader().fromFile(original);
                ImmutableImage thumb = img.cover(315, 475);

                File thumbFile = new File(tempDir, "thumb.webp");
                WebpWriter writer = WebpWriter.DEFAULT.withQ(80); // 품질 80%
                thumb.output(writer, thumbFile);

                // 3. S3 업로드 (mainThumbnailUrl 갱신 or 기존 덮어쓰기)
                String newThumbUrl = s3Uploader.uploadWithKey(thumbFile, "animes/" + animeId + "/thumb.webp");
                anime.updateImage(mainImageUrl, newThumbUrl);

                animeRepository.save(anime);

                System.out.println("✅ 썸네일 갱신 성공, id: " + animeId);

            } catch (Exception e) {
                System.out.println("⚠️ 썸네일 처리 실패, id: " + animeId);
                e.printStackTrace();
            }
        }
    }
}
