package com.duckstar.web.dto;

import com.duckstar.domain.enums.OttType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class OttDto {
    OttType ottType;

    String watchUrl;
}
