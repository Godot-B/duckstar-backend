package com.duckstar.service;

import com.duckstar.domain.Week;
import com.duckstar.repository.AnimeCandidate.AnimeCandidateRepository;
import com.duckstar.web.dto.WeekResponseDto;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.duckstar.web.dto.VoteResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

    private final WeekService weekService;
    private final AnimeCandidateRepository animeCandidateRepository;

    public AnimeCandidateListDto getAnimeCandidateList() {
        Week currentWeek = weekService.getCurrentWeek();

        return AnimeCandidateListDto.builder()
                .weekDto(WeekDto.from(currentWeek))
                .animeCandidates(
                        animeCandidateRepository.getAnimeCandidateDtosByWeekId(currentWeek.getId())
                )
                .build();
    }
}
