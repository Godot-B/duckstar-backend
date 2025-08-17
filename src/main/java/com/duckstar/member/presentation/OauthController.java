package com.duckstar.member.presentation;

import com.duckstar.member.domain.OauthServerType;
import com.duckstar.member.service.OauthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OauthController {

    private final OauthService oauthService;

    @GetMapping("/{oauthServerType}")
    ResponseEntity<Void> redirectAuthCodeRequestUrl(
            @PathVariable OauthServerType oauthServerType
    ) {
        String redirectUrl = oauthService.getAuthCodeRequestUrl(oauthServerType);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }

    @GetMapping("/login/{oauthServerType}")
    ResponseEntity<Long> login(@PathVariable OauthServerType oauthServerType,
                               @RequestParam String code) {

        Long loginMemberId = oauthService.login(oauthServerType, code);
        return ResponseEntity.ok(loginMemberId);
    }
}
