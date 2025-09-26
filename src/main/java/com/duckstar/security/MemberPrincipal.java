package com.duckstar.security;

import com.duckstar.domain.Member;
import com.duckstar.security.domain.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class MemberPrincipal implements OAuth2User {
    private final Long id;
    private final Collection<? extends GrantedAuthority> authorities;

    private MemberPrincipal(
            Long id,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.authorities = List.copyOf(authorities);
    }

    public static MemberPrincipal of(Member member) {
        return new MemberPrincipal(
                member.getId(),
                createAuthorities(member.getRole())
        );
    }

    private static List<GrantedAuthority> createAuthorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();    // 사용 X
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(auth ->
                        "ROLE_ADMIN".equals(auth.getAuthority()));
    }
}
