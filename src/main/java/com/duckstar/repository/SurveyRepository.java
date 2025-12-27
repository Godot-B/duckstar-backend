package com.duckstar.repository;

import com.duckstar.domain.Survey;
import com.duckstar.domain.enums.SurveyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
    List<Survey> findAllByStatus(SurveyStatus status);

    List<Survey> findAllByStatusIn(Collection<SurveyStatus> statuses);
}