package com.duckstar.security;

import com.duckstar.security.domain.Member;
import com.duckstar.security.domain.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class MemberPrincipal implements OAuth2User {
    private final Long id;
    private final Role role;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    private MemberPrincipal(
            Long id,
            Role role,
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes
    ) {
        this.id = id;
        this.role = role;
        this.authorities = List.copyOf(authorities);
        this.attributes = Map.copyOf(attributes);
    }

    public static MemberPrincipal of(Member member, Map<String, Object> attributes) {
        return new MemberPrincipal(
                member.getId(),
                member.getRole(),
                createAuthorities(member.getRole()),
                attributes
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
        return (A) attributes.get(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
