package com.duckstar.web.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class WeekDto {

    Integer year;

    Integer quarter;

    Integer week;

    LocalDate startDate;

    LocalDate endDate;
}
