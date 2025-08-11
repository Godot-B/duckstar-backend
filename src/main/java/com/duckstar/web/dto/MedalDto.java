package com.duckstar.web.dto;

import com.duckstar.domain.enums.MedalType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MedalDto {

    MedalType type;

    Integer rank;

    Integer year;

    Integer quarter;

    Integer week;
}
