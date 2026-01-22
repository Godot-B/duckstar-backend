package com.duckstar.repository.Reply;

import com.duckstar.security.MemberPrincipal;

import java.util.List;

import static com.duckstar.web.dto.CommentResponseDto.*;

public interface ReplyRepositoryCustom {
    List<ReplyDto> getReplyDtos(
            Long commentId,
            MemberPrincipal principal,
            int offset,
            int limit
    );
}
