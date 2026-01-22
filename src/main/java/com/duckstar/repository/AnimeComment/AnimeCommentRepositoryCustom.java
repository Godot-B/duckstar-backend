package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;

import java.util.List;

public interface AnimeCommentRepositoryCustom {
    List<CommentDto> getCommentDtos(
            Long animeId,
            List<Long> episodeIds,
            CommentSortType sortBy,
            MemberPrincipal principal,
            int offset,
            int limit
    );

    Integer countTotalElements(Long animeId, List<Long> episodeIds);
}
