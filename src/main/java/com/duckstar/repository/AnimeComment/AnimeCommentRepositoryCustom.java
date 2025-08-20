package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnimeCommentRepositoryCustom {
    List<CommentDto> getCommentDtos(
            Long animeId,
            List<Long> episodeIds,
            CommentSortType sortBy,
            Pageable pageable,
            MemberPrincipal principal
    );
}
