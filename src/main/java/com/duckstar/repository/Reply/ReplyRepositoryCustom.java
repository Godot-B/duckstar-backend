package com.duckstar.repository.Reply;

import com.duckstar.security.MemberPrincipal;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.duckstar.web.dto.CommentResponseDto.*;

public interface ReplyRepositoryCustom {
    List<ReplyDto> getReplyDtos(
            Long commentId,
            Pageable pageable,
            MemberPrincipal principal
    );
}
