package com.duckstar.abroad.reader;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import com.duckstar.apiPayload.exception.handler.WeekHandler;
import com.duckstar.abroad.aniLab.Anilab;
import com.duckstar.abroad.aniLab.AnilabRepository;
import com.duckstar.abroad.animeCorner.AnimeCorner;
import com.duckstar.abroad.animeCorner.AnimeCornerRepository;
import com.duckstar.domain.*;
import com.duckstar.domain.Character;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.AnimeCharacter;
import com.duckstar.domain.mapping.AnimeSeason;
import com.duckstar.domain.mapping.surveyVote.SurveyCandidate;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.repository.*;
import com.duckstar.repository.AnimeCharacter.AnimeCharacterRepository;
import com.duckstar.repository.AnimeSeason.AnimeSeasonRepository;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.SurveyCandidate.SurveyCandidateRepository;
import com.duckstar.repository.Week.WeekRepository;
import com.duckstar.s3.S3Uploader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.duckstar.web.dto.admin.CsvRequestDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CsvImportService {
    private final AnimeRepository animeRepository;
    private final CharacterRepository characterRepository;
    private final AnimeCharacterRepository animeCharacterRepository;
    private final S3Uploader s3Uploader;
    private final EpisodeRepository episodeRepository;
    private final QuarterRepository quarterRepository;
    private final AnimeSeasonRepository animeSeasonRepository;
    private final WeekRepository weekRepository;
    private final AnimeCornerRepository animeCornerRepository;
    private final AnilabRepository anilabRepository;
    private final SurveyRepository surveyRepository;
    private final SurveyCandidateRepository surveyCandidateRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    static {
        ImageIO.scanForPlugins();
        String[] formats = ImageIO.getWriterFormatNames();
        System.out.println("지원하는 포맷: " + Arrays.toString(formats));
    }

    public void importAnimeCorner(Long weekId, MultipartFile animeCornerCsv) throws IOException {
        if (animeCornerCsv == null || animeCornerCsv.isEmpty()) {
            return;
        }

        Week week = weekRepository.findWeekById(weekId)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        Reader reader = new InputStreamReader(
                new BOMInputStream(animeCornerCsv.getInputStream()), StandardCharsets.UTF_8);

        CSVFormat format = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()   // 첫 줄을 헤더로 인식
                .withTrim()                  // 공백 제거
                .withIgnoreEmptyLines();     // 빈 줄 무시

        CSVParser parser = new CSVParser(reader, format);

        Map<String, Anime> idToTitleMap = animeRepository.findAll().stream()
                .filter(a -> a.getTitleEng() != null)
                .collect(Collectors.toMap(
                        Anime::getTitleEng,
                        a -> a,
                        (a1, a2) -> a1
                ));

        for (CSVRecord record : parser) {
            String titleEng = record.get("title_eng");
            Anime anime = idToTitleMap.getOrDefault(titleEng, null);
            String mainThumbnailUrl;
//            if (anime == null) {
//                mainThumbnailUrl = record.get("mainThumbnailUrl");
//            } else {
//                mainThumbnailUrl = anime.getMainThumbnailUrl();
//            }

            Integer rank;
            try {
                rank = Integer.parseInt(record.get("rank"));
            } catch (NumberFormatException e) {
                rank = null;
            }

            Integer rankDiff;
            try {
                rankDiff = Integer.parseInt(record.get("rank_diff"));
            } catch (NumberFormatException e) {
                rankDiff = null;
            }

            Integer consecutiveWeeksAtSameRank;
            try {
                consecutiveWeeksAtSameRank = Integer.parseInt(record.get("consecutive_weeks_at_same_rank"));
            } catch (NumberFormatException e) {
                consecutiveWeeksAtSameRank = null;
            }

            AnimeCorner animeCorner = AnimeCorner.builder()
                    .week(week)
                    .anime(anime)
                    .title(titleEng)
                    .rank(rank)
                    .rankDiff(rankDiff == null ? 0 : rankDiff)
                    .consecutiveWeeksAtSameRank(
                            consecutiveWeeksAtSameRank == null ? 0 : consecutiveWeeksAtSameRank)
                    .build();

            animeCornerRepository.save(animeCorner);
        }
    }

    public void importAnilab(Long weekId, MultipartFile anilabCsv) throws IOException {
        if (anilabCsv == null || anilabCsv.isEmpty()) {
            return;
        }

        Week week = weekRepository.findWeekById(weekId)
                .orElseThrow(() -> new WeekHandler(ErrorStatus.WEEK_NOT_FOUND));

        Reader reader = new InputStreamReader(anilabCsv.getInputStream(), StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.Builder
                .create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();
        CSVParser parser = new CSVParser(reader, format);

        Map<String, Anime> idToTitleMap = animeRepository.findAll().stream()
                .filter(a -> a.getTitleOrigin() != null)
                .collect(Collectors.toMap(
                        Anime::getTitleOrigin,
                        a -> a,
                        (a1, a2) -> a1
                ));

        for (CSVRecord record : parser) {
            String titleOrigin = record.get("title");
            Anime anime = idToTitleMap.getOrDefault(titleOrigin, null);
            String mainThumbnailUrl;
            if (anime == null) {
                mainThumbnailUrl = record.get("mainThumbnailUrl");
            } else {
                mainThumbnailUrl = null;
            }

            Integer rank;
            try {
                rank = Integer.parseInt(record.get("rank"));
            } catch (NumberFormatException e) {
                rank = null;
            }

            Integer rankDiff;
            try {
                rankDiff = Integer.parseInt(record.get("rankDiff"));
            } catch (NumberFormatException e) {
                rankDiff = null;
            }

            Anilab anilab = Anilab.builder()
                    .week(week)
                    .anime(anime)
                    .mainThumbnailUrl(mainThumbnailUrl)
                    .title(titleOrigin)
                    .rank(rank)
                    .rankDiff(rankDiff == null ? 0 : rankDiff)
                    .consecutiveWeeksAtSameRank(
                            null  // 덕스타에서 세팅
                    )
                    .build();

            anilabRepository.save(anilab);
        }
    }

    private File downloadImage(String imageUrl, File tempDir, String name) throws IOException {
        // 1. 확장자 추출 (없으면 기본 jpg)
        String extension = "jpg";
        String path = new URL(imageUrl).getPath();
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx != -1) {
            String ext = path.substring(dotIdx + 1).toLowerCase();
            if (List.of("jpg", "jpeg", "png", "webp").contains(ext)) {
                extension = ext;
            }
        }

        // 2. 저장 파일 경로
        File outputFile = new File(tempDir, name + "." + extension);

        // 3. 단순 다운로드
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return outputFile;
    }

    public void importCandidates(Long surveyId, MultipartFile csv) throws IOException {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(() ->
                new VoteHandler(ErrorStatus.SURVEY_NOT_FOUND));

        Map<String, Long> animeMap = animeRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Anime::getTitleKor,
                        Anime::getId
                ));

        Reader reader = new InputStreamReader(csv.getInputStream(), StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.Builder
                .create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        CSVParser parser = new CSVParser(reader, format);
        for (CSVRecord record : parser) {
            try {
                Medium medium;
                try {
                    medium = Medium.valueOf(record.get("medium"));
                } catch (IllegalArgumentException e) {
                    medium = null;
                }

                Quarter quarter = quarterRepository
                        .getReferenceById(Long.valueOf(record.get("quarterId")));

                String mainImageUrl = record.get("mainImageUrl");
                boolean isAlreadyUploaded = mainImageUrl.startsWith("https://" + bucket + "/");
                if (isAlreadyUploaded) {
                    String[] parts = mainImageUrl.split("/");
                    Anime animeRef = animeRepository.getReferenceById(Long.valueOf(parts[4]));
                    SurveyCandidate candidate = SurveyCandidate.createByAnime(
                            survey,
                            quarter,
                            animeRef
                    );

                    surveyCandidateRepository.save(candidate);

                } else { // ⚠️ Survey 전용 candidate 이미지 업로드 (애니메이션과 관계 맺지 않은 경우)
                    String title = record.get("titleKor");
                    SurveyCandidate candidate = SurveyCandidate.create(
                            survey,
                            quarter,
                            null,
                            title,
                            null,
                            null,
                            medium
                    );

                    Long animeId = animeMap.get(title);
                    if (animeId != null) {
                        Anime anime = animeRepository.getReferenceById(animeId);
                        candidate.setAnime(anime);
                    }
                    SurveyCandidate saved = surveyCandidateRepository.save(candidate);

                    try {
                        // S3 업로드 & image 필드 업데이트
                        uploadCandidateMain(mainImageUrl, saved);
                    } catch (Exception e) {
                        log.warn("⚠️ 이미지 처리 실패 - id: {}, url: {}", saved.getId(), mainImageUrl, e);
                    }
                }

            } catch (Exception e) {
                log.error("❌ CSV 레코드 처리 실패: {}", record, e);
            }
        }
    }

    public void importNewSeason(Integer year, Integer quarter, NewSeasonRequestDto request) throws IOException {

    }

    void importEpisodes(
            MultipartFile episodesCsv,
            Map<Long, Anime> animeMap,
            Map<Integer, Long> animeIdMap
    ) throws IOException {
        Reader reader = new InputStreamReader(episodesCsv.getInputStream(), StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.Builder
                .create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        CSVParser parser = new CSVParser(reader, format);

        List<Episode> rows = new ArrayList<>();
        for (CSVRecord record : parser) {
            Integer episodeNumber;
            try {
                episodeNumber = Integer.valueOf(record.get("episode_number"));
            } catch (NumberFormatException e) {
                episodeNumber = null;
            }

            LocalDateTime scheduledAt;
            try {
                scheduledAt = LocalDateTime.parse(record.get("scheduled_at"));
            } catch (IllegalArgumentException e) {
                scheduledAt = null;
            }
            LocalDateTime nextEpScheduledAt;

            try {
                nextEpScheduledAt = LocalDateTime.parse(record.get("next_ep_scheduled_at"));
            } catch (IllegalArgumentException e) {
                nextEpScheduledAt = null;
            }

            boolean isLastEpisode = Boolean.parseBoolean(record.get("is_last_episode"));

            Episode episode = Episode.create(
                    animeMap.get(animeIdMap.get(Integer.valueOf(record.get("anime_id")))),
                    episodeNumber,
                    scheduledAt,
                    nextEpScheduledAt,
                    isLastEpisode
            );
            rows.add(episode);
        }
        episodeRepository.saveAll(rows);
    }

    Map<Long, Anime> importAnimeCharacters(
            MultipartFile animeCharactersCsv,
            Map<Integer, Long> animeIdMap,
            Map<Integer, Long> characterIdMap
    ) throws IOException {

        Reader reader = new InputStreamReader(animeCharactersCsv.getInputStream(), StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.Builder
                .create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        Map<Long, Anime> animeMap = animeRepository.findAllById(animeIdMap.values()).stream()
                .collect(Collectors.toMap(Anime::getId, a -> a));
        Map<Long, Character> characterMap = characterRepository.findAllById(characterIdMap.values()).stream()
                .collect(Collectors.toMap(Character::getId, c -> c));

        CSVParser parser = new CSVParser(reader, format);

        List<AnimeCharacter> rows = new ArrayList<>();
        for (CSVRecord record : parser) {
            Integer animeOldId = Integer.valueOf(record.get("anime_id"));
            Integer characterOldId = Integer.valueOf(record.get("character_id"));
            AnimeCharacter animeCharacter = AnimeCharacter.builder()
                    .anime(animeMap.get(animeIdMap.get(animeOldId)))
                    .character(characterMap.get(characterIdMap.get(characterOldId)))
                    .build();

            rows.add(animeCharacter);
        }
        animeCharacterRepository.saveAll(rows);

        return animeMap;
    }

    private String normalizeUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url; // 온나다는 https만 지원하니까 https 강제
        }
        return url;
    }

    public Map<Integer, Long> importCharacters(MultipartFile charactersCsv) throws IOException {
        HashMap<Integer, Long> idMap = new HashMap<>();

        Reader reader = new InputStreamReader(charactersCsv.getInputStream(), StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.Builder
                .create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();
        CSVParser parser = new CSVParser(reader, format);

        for (CSVRecord record : parser) {
            try {
                int oldId = Integer.parseInt(record.get("id"));

                Character character = Character.builder()
                        .nameKor(record.get("nameKor"))
                        .nameKanji(record.get("nameKanji"))
                        .nameEng(record.get("nameEng"))
                        .cv(record.get("cv"))
                        .build();

                Character saved = characterRepository.save(character);

                String mainUrl = record.get("mainImageUrl");
                mainUrl = normalizeUrl(mainUrl);
                String thumbUrl = record.get("mainThumbnailUrl");
                thumbUrl = normalizeUrl(thumbUrl);

                Long newId = saved.getId();
                try {
                    newId = uploadAndUpdateCharacter(mainUrl, thumbUrl, saved);
                } catch (Exception e) {
                    log.warn("⚠️ 이미지 처리 실패 - id: {}", oldId, e);
                } finally {
                    idMap.put(oldId, newId);
                }

            } catch (Exception e) {
                log.error("❌ CSV 레코드 처리 실패: {}", record, e);
            }
        }

        return idMap;
    }

    private Long uploadAndUpdateCharacter(String main, String thumb, Character character) throws IOException {
        File tempDir = Files.createTempDirectory("character").toFile();
        try {
            Long newId = character.getId();

            String mainWebpUrl = null;
            if (main != null) {
                File mainWebp = downloadAndConvertToWebp(main, tempDir, "main");
                String mainS3Key = "characters/" + newId + "/main.webp";
                mainWebpUrl = s3Uploader.uploadWithKey(mainWebp, mainS3Key);
            }

            String thumbWebpUrl = null;
            if (thumb != null) {
                File thumbWebp = downloadAndConvertToWebp(thumb, tempDir, "thumb");
                String thumbS3Key = "characters/" + newId + "/thumb.webp";
                thumbWebpUrl = s3Uploader.uploadWithKey(thumbWebp, thumbS3Key);
            }

            character.updateImage(mainWebpUrl, thumbWebpUrl);

            return newId;

        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        } finally {
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    private File downloadAndConvertToWebp(
            String imageUrl, File tempDir, String name
    ) throws IOException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, KeyManagementException {

        URL url = new URL(imageUrl);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

        // 1. HTTPS 연결 설정 (온나다 다운로드에만 커스텀 Truststore 적용)
        if (url.getHost().contains("onnada.com")) {
            char[] password = "secret".toCharArray();
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("truststore.p12")) {
                if (is == null) {
                    throw new IllegalStateException("truststore.p12 not found in resources");
                }
                trustStore.load(is, password);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

            conn.setSSLSocketFactory(sslContext.getSocketFactory());
        }

        // 3. 원본 파일 저장
        String extension = "jpg";
        String path = url.getPath();
        int dotIdx = path.lastIndexOf('.');
        if (dotIdx != -1) {
            String ext = path.substring(dotIdx + 1).toLowerCase();
            if (List.of("jpg", "jpeg", "png", "webp").contains(ext)) {
                extension = ext;
            }
        }

        File original = new File(tempDir, name + "." + extension);
        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, original.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        // 4. WebP 변환,
        // 5. 원본 삭제
        return convertToWebpAndGet(tempDir, name, original);
    }

    private static File convertToWebpAndGet(File tempDir, String name, File original) throws IOException {
        try {
            ImmutableImage image = ImmutableImage.loader().fromFile(original);

            // 이미지가 너무 크면 리사이즈 (최대 2048x2048)
            int maxDimension = 2048;
            if (image.width > maxDimension || image.height > maxDimension) {
                double scale = Math.min((double) maxDimension / image.width, (double) maxDimension / image.height);
                int newWidth = (int) (image.width * scale);
                int newHeight = (int) (image.height * scale);
                image = image.scaleTo(newWidth, newHeight);
            }

            File webpFile = new File(tempDir, name + ".webp");
            WebpWriter writer = WebpWriter.DEFAULT.withQ(80);  // 품질 80%
            image.output(writer, webpFile);
            return webpFile;
        } finally {
            original.delete();
        }
    }

    public static File convertToWebpAndGet(File tempDir, String name, InputStream in) throws IOException {
        File original = new File(tempDir, name + ".tmp");
        Files.copy(in, original.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return convertToWebpAndGet(tempDir, name, original);
    }

    public Map<Integer, Long> importAnimes(Season season, MultipartFile animeCsv) throws IOException {
        HashMap<Integer, Long> idMap = new HashMap<>();

        Reader reader = new InputStreamReader(animeCsv.getInputStream(), StandardCharsets.UTF_8);
        CSVFormat format = CSVFormat.Builder
                .create()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();
        CSVParser parser = new CSVParser(reader, format);

        for (CSVRecord record : parser) {
            try {
                int oldId = Integer.parseInt(record.get("id"));

                Medium medium;
                try {
                    medium = Medium.valueOf(record.get("medium"));
                } catch (IllegalArgumentException e) {
                    medium = null;
                }

                AnimeStatus status;
                try {
                    status = AnimeStatus.valueOf(record.get("status"));
                } catch (IllegalArgumentException e) {
                    status = null;
                }

                Integer totalEpisodes;
                try {
                    totalEpisodes = Integer.valueOf(record.get("totalEpisodes"));
                } catch (NumberFormatException e) {
                    totalEpisodes = null;
                }

                LocalDateTime premiereDateTime = null;
                try {
                    String dateStr = record.get("premiereDateTime");
                    if (dateStr != null && !dateStr.isBlank()) {
                        premiereDateTime = LocalDateTime.parse(dateStr);
                    }
                } catch (Exception ignored) {
                }

                DayOfWeekShort dayOfWeek;
                try {
                    dayOfWeek = DayOfWeekShort.valueOf(record.get("dayOfWeek"));
                } catch (IllegalArgumentException e) {
                    dayOfWeek = null;
                }

                int minAge;
                try {
                    minAge = Integer.parseInt(record.get("minAge"));
                } catch (NumberFormatException e) {
                    minAge = 0;
                }

                String jsonString = record.get("officialSite");
                ObjectMapper objectMapper = new ObjectMapper();

                Map<SiteType, String> officialSiteMap =
                        objectMapper.readValue(jsonString, new TypeReference<>() {
                        });

                Anime anime = Anime.builder()
                        .medium(medium)
                        .status(status)
                        .totalEpisodes(totalEpisodes)
                        .premiereDateTime(premiereDateTime)
                        .titleKor(record.get("titleKor"))
                        .titleOrigin(record.get("titleOrigin"))
                        .titleEng(record.get("titleEng"))
                        .dayOfWeek(dayOfWeek)
                        .airTime(record.get("airTime"))
                        .corp(record.get("corp"))
                        .director(record.get("director"))
                        .genre(record.get("genre"))
                        .author(record.get("author"))
                        .minAge(minAge)
                        .officialSite(officialSiteMap)
//                    .synopsis(record.get("synopsis"))
                        .build();

                Anime saved = animeRepository.save(anime);

                animeSeasonRepository.save(AnimeSeason.create(saved, season));

                String imageUrl = record.get("mainImageUrl");

                Long newId = saved.getId();

                try {
                    // S3 업로드 & image 필드 업데이트
                    newId = uploadAnimeMain(imageUrl, saved);
                } catch (Exception e) {
                    log.warn("⚠️ 이미지 처리 실패 - id: {}, url: {}", oldId, imageUrl, e);
                } finally {
                    idMap.put(oldId, newId);
                }
            } catch (Exception e) {
                log.error("❌ CSV 레코드 처리 실패: {}", record, e);
            }
        }

        return idMap;
    }

    private Long uploadAnimeMain(String imageUrl, Anime anime) throws IOException {
        File tempDir = Files.createTempDirectory("anime").toFile();
        try {
            // 메인 webp 변환
            File mainWebp = downloadAndConvertToWebp(imageUrl, tempDir, "main");

            // 썸네일 생성 (minWidth 640 보장, 비율 유지)
            File thumbWebp = s3Uploader.createThumbnail(mainWebp, 640);

            return uploadAnimeImages(anime, mainWebp, thumbWebp, 1);

        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        } finally {
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    private Long uploadCandidateMain(String imageUrl, SurveyCandidate candidate) throws IOException {
        File tempDir = Files.createTempDirectory("candidate").toFile();
        try {
            // 메인 webp 변환
            File mainWebp = downloadAndConvertToWebp(imageUrl, tempDir, "main");

            // 썸네일 생성 (minWidth 640 보장, 비율 유지)
            File thumbWebp = s3Uploader.createThumbnail(mainWebp, 640);

            return uploadCandidateImages(candidate, mainWebp, thumbWebp, 1);

        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        } finally {
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    private Long uploadCandidateImages(
            SurveyCandidate candidate,
            File mainWebp,
            File thumbWebp,
            int version
    ) {
        // S3 업로드
        Long id = candidate.getId();

        String tail = version >= 2 ? "_v" + version + ".webp" : ".webp";

        String mainS3Key = "candidates/" + id + "/main" + tail;
        String thumbS3Key = "candidates/" + id + "/thumb" + tail;

        String mainUrl = s3Uploader.uploadWithKey(mainWebp, mainS3Key);
        String thumbUrl = s3Uploader.uploadWithKey(thumbWebp, thumbS3Key);

        candidate.updateImage(mainUrl, thumbUrl);

        return id;
    }

    public void uploadAnimeMain(MultipartFile main, Anime anime) throws IOException {
        File tempDir = Files.createTempDirectory("anime").toFile();
        try {
            // 메인 webp 변환
            File mainWebp = convertToWebpAndGet(tempDir, "main", main.getInputStream());

            // 썸네일 생성 (minWidth 640 보장, 비율 유지)
            File thumbWebp = s3Uploader.createThumbnail(mainWebp, 640);

            uploadAnimeImages(anime, mainWebp, thumbWebp, 1);

        } finally {
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    public void updateAnimeMain(MultipartFile main, int version, Anime anime) throws IOException {
        File tempDir = Files.createTempDirectory("anime").toFile();
        try {
            // 메인 webp 변환
            File mainWebp = convertToWebpAndGet(tempDir, "main", main.getInputStream());

            // 썸네일 생성 (minWidth 640 보장, 비율 유지)
            File thumbWebp = s3Uploader.createThumbnail(mainWebp, 640);

            uploadAnimeImages(anime, mainWebp, thumbWebp, version);

        } finally {
            FileSystemUtils.deleteRecursively(tempDir);
        }
    }

    private Long uploadAnimeImages(
            Anime anime,
            File mainWebp,
            File thumbWebp,
            int version
    ) {
        // S3 업로드
        Long newId = anime.getId();

        String tail = version >= 2 ? "_v" + version + ".webp" : ".webp";

        String mainS3Key = "animes/" + newId + "/main" + tail;
        String thumbS3Key = "animes/" + newId + "/thumb" + tail;

        String mainUrl = s3Uploader.uploadWithKey(mainWebp, mainS3Key);
        String thumbUrl = s3Uploader.uploadWithKey(thumbWebp, thumbS3Key);

        anime.updateImage(mainUrl, thumbUrl);

        return newId;
    }
}
