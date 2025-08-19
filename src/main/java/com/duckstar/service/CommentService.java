package com.duckstar.service;

import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.web.dto.CommentRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final AnimeRepository animeRepository;

    @Transactional
    public void leaveAnimeComment(
            Long animeId,
            CommentRequestDto request,
            Long memberId
    ) {
        AnimeComment.create(

        )
    }
}
