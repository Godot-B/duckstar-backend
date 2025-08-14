package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PageInfo {

    Boolean hasNext;

    Integer page;

    Integer size;
}
