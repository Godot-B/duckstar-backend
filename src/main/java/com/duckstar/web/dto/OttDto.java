package com.duckstar.web.dto;

import com.duckstar.domain.enums.OttType;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OttDto {
    OttType ottType;

    String watchUrl;
}
