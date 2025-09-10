package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.apiPayload.exception.handler.MemberHandler;
import com.duckstar.domain.Member;
import com.duckstar.s3.S3Uploader;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.web.dto.MemberRequestDto;
import com.duckstar.web.dto.MemberResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.duckstar.web.dto.MemberRequestDto.*;
import static com.duckstar.web.dto.MemberResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    public Member findByIdOrThrow(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    public MePreviewDto getCurrentUser(Long memberId) {
        Member member = findByIdOrThrow(memberId);
        return MePreviewDto.of(member);
    }

    @Transactional
    public UpdateReceiptDto updateProfile(ProfileRequestDto request, MemberPrincipal principal) {
        if (principal == null) {
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);
        }

        Member member = findByIdOrThrow(principal.getId());
        String nickname = member.getNickname();
        String profileImageUrl = member.getProfileImageUrl();

        String reqNickname = request.getNickname();
        if (StringUtils.hasText(reqNickname)) {
            nickname = reqNickname;
        }

        MultipartFile reqImage = request.getImage();
        if (reqImage != null && !reqImage.isEmpty()) {
            s3Uploader.delete(profileImageUrl);
            profileImageUrl = s3Uploader.uploadWithUUID(reqImage, "members");
        }

        boolean nicknameChanged = !Objects.equals(member.getNickname(), nickname);
        boolean imageChanged = !Objects.equals(member.getProfileImageUrl(), profileImageUrl);

        boolean isChanged = nicknameChanged || imageChanged;
        if (isChanged) {
            member.updateProfile(nickname, profileImageUrl);
        }

        return UpdateReceiptDto.builder()
                .isChanged(isChanged)
                .mePreviewDto(
                        MePreviewDto.of(member)
                )
                .build();
    }
}
