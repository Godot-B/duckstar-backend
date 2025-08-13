package com.duckstar.web.dto;

import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class QuarterDto {

    Integer year;

    Integer quarter;

    List<WeekDto> weekDtos;
}
