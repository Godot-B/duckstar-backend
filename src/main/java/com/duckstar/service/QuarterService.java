package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.QuarterHandler;
import com.duckstar.domain.Quarter;
import com.duckstar.repository.QuarterRepository;
import com.duckstar.util.QuarterUtil;
import com.duckstar.web.dto.SearchResponseDto;
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
    public List<Quarter> getOrCreateQuartersByEdges(
            YQWRecord firstEpWeekRecord,
            YQWRecord lastEpWeekRecord
    ) {
        YQWRecord currentQuarterRecord = firstEpWeekRecord;
        if (lastEpWeekRecord == null) {
            Quarter quarter = getOrCreateQuarter(
                    currentQuarterRecord.yearValue(),
                    currentQuarterRecord.quarterValue()
            );
            return Collections.singletonList(quarter);
        }

        // 걸치는 마지막 분기가 특정 분기의 4주차 이상이라면 포함
        boolean isLastQuarterIncluded = lastEpWeekRecord.weekValue() >= 4;
        YQWRecord lastQuarterRecord = isLastQuarterIncluded ?
                lastEpWeekRecord :
                lastEpWeekRecord.getPreviousQuarterRecord();

        List<Quarter> quarters = new ArrayList<>();
        while (currentQuarterRecord.yearValue() != lastQuarterRecord.yearValue() ||
                currentQuarterRecord.quarterValue() != lastQuarterRecord.quarterValue()) {

            quarters.add(
                    getOrCreateQuarter(
                            currentQuarterRecord.yearValue(),
                            currentQuarterRecord.quarterValue()
                    )
            );

            currentQuarterRecord = currentQuarterRecord.getNextQuarterRecord();
        }
        return quarters;
    }
}
