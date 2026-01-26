package com.duckstar.service;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.mapping.AnimeQuarter;
import com.duckstar.repository.AnimeQuarter.AnimeQuarterRepository;
import com.duckstar.repository.QuarterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.duckstar.util.QuarterUtil.*;
import static com.duckstar.web.dto.SearchResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuarterService {
    private final QuarterRepository quarterRepository;
    private final AnimeQuarterRepository animeQuarterRepository;

    public List<QuarterResponseDto> getQuarters() {
        return quarterRepository.findAll().stream()
                .filter(Quarter::getIsPrepared)
                .sorted(
                        Comparator.comparing(Quarter::getYearValue).reversed()
                                .thenComparing(Quarter::getQuarterValue, Comparator.reverseOrder())
                )
                .collect(Collectors.groupingBy(
                        Quarter::getYearValue,
                        LinkedHashMap::new,
                        Collectors.mapping(Quarter::getQuarterValue, Collectors.toList())
                ))
                .entrySet().stream()
                .map(e -> QuarterResponseDto.builder()
                        .year(e.getKey())
                        .quarters(e.getValue())
                        .build()
                )
                .toList();
    }

    @Transactional
    public Quarter getOrCreateQuarter(int thisYearValue, int thisQuarterValue) {
        return quarterRepository.findByYearValueAndQuarterValue(thisYearValue, thisQuarterValue)
                .orElseGet(() -> quarterRepository.save(Quarter.create(thisYearValue, thisQuarterValue)));
    }

    @Transactional
    public void saveQuartersBetweenEdges(
            Anime anime,
            YQWRecord firstEpWeekRecord,
            YQWRecord lastEpWeekRecord
    ) {
        YQWRecord currentQuarterRecord = firstEpWeekRecord;
        List<Quarter> quarters = new ArrayList<>();
        quarters.add(
                getOrCreateQuarter(
                        currentQuarterRecord.yearValue(),
                        currentQuarterRecord.quarterValue()
                )
        );

        if (lastEpWeekRecord != null) {
            // 걸치는 마지막 분기가 특정 분기의 4주차 이상이라면 포함
            boolean isLastQuarterIncluded = lastEpWeekRecord.weekValue() >= 4;
            YQWRecord lastQuarterRecord = isLastQuarterIncluded ?
                    lastEpWeekRecord :
                    lastEpWeekRecord.getPreviousQuarterRecord();
            while (currentQuarterRecord.yearValue() != lastQuarterRecord.yearValue() ||
                    currentQuarterRecord.quarterValue() != lastQuarterRecord.quarterValue()) {
                currentQuarterRecord = currentQuarterRecord.getNextQuarterRecord();

                quarters.add(
                        getOrCreateQuarter(
                                currentQuarterRecord.yearValue(),
                                currentQuarterRecord.quarterValue()
                        )
                );
            }
        }

        List<AnimeQuarter> animeQuarters = quarters.stream()
                .map(q -> AnimeQuarter.create(anime, q))
                .toList();

        animeQuarterRepository.saveAll(animeQuarters);
    }
}
