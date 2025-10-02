package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.QuarterHandler;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Season;
import com.duckstar.repository.QuarterRepository;
import com.duckstar.repository.SeasonRepository;
import com.duckstar.util.QuarterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.duckstar.util.QuarterUtil.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonService {
    private final QuarterRepository quarterRepository;
    private final SeasonRepository seasonRepository;

    // CSV 리더 설계 이후 다시 돌아와 아래 로직들 검증

    public Quarter getOrCreateQuarter(
            boolean createEnabled,
            int thisYearValue,
            int thisQuarterValue
    ) {
        Optional<Quarter> quarterOpt =
                quarterRepository.findByYearValueAndQuarterValue(thisYearValue, thisQuarterValue);

        Quarter quarter;
        if (quarterOpt.isPresent()) {
            quarter = quarterOpt.get();

            // 분기 변경 주 && DB에 없을 때
        } else if (createEnabled) {
            quarter = Quarter.create(thisYearValue, thisQuarterValue);
            quarter = quarterRepository.save(quarter);
        } else {
            throw new QuarterHandler(ErrorStatus.QUARTER_NOT_FOUND);
        }
        return quarter;
    }

    public Season getOrCreateSeason(boolean createEnabled, Quarter quarter) {
        int thisYearValue = quarter.getYearValue();
        int thisQuarterValue = quarter.getQuarterValue();

        Optional<Season> seasonOpt =
                seasonRepository.findByYearValueAndQuarter_QuarterValue(thisYearValue, thisQuarterValue);
        Season season;
        if (seasonOpt.isPresent()) {
            season = seasonOpt.get();

            // 분기 변경 주 && DB에 없을 때
        } else if (createEnabled) {
            season = Season.create(thisYearValue, quarter);
            season = seasonRepository.save(season);
        } else {
            throw new QuarterHandler(ErrorStatus.SEASON_NOT_FOUND);
        }
        return season;
    }
}
