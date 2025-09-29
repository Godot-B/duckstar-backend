package com.duckstar.security.config;

import com.duckstar.security.jwt.JwtAuthenticationFilter;
import com.duckstar.security.oauth2.CustomOAuth2AccessTokenResponseConverter;
import com.duckstar.security.oauth2.CustomOAuth2UserService;
import com.duckstar.security.oauth2.UserLoginSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final UserLoginSuccessHandler userLoginSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> customAccessTokenResponseClient() {

        // *** Spring Security 6.6이상 버전 업 시에는 WebClient 기반으로 마이그레이션 필요 ***

        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();

        OAuth2AccessTokenResponseHttpMessageConverter messageConverter =
                new OAuth2AccessTokenResponseHttpMessageConverter();
        messageConverter.setAccessTokenResponseConverter(new CustomOAuth2AccessTokenResponseConverter());

        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(),
                messageConverter
        ));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());

        client.setRestOperations(restTemplate);
        return client;
    }

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
                return customize(authRequest);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
                return customize(authRequest);
            }

            private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authRequest) {
                if (authRequest == null) return null;
                return OAuth2AuthorizationRequest.from(authRequest)
                        .additionalParameters(params -> {
                            params.put("access_type", "offline");
                            params.put("prompt", "login");
                        })
                        .build();
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> customClient,
            OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsSource()))
                .sessionManagement(
                        sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/signup", "/css/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**",

                                // 🔑 OAuth 엔드포인트 허용
                                "/login/**", "/oauth2/**"
                                ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/animes/*").authenticated()

                        .requestMatchers(
                                "/api/v1/vote/**",
                                "/api/v1/search/**",
                                "/api/v1/animes/**",
                                "/api/v1/home/**",
                                "/api/v1/chart/**",
                                "/api/v1/csv/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/comments/*/replies/**").permitAll()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth ->
                        oauth
                                .authorizationEndpoint(authz -> authz
                                        .authorizationRequestResolver(customAuthorizationRequestResolver)
                                )
                                // 1. 소셜 사용자 → 우리 Member 매핑 (회원가입/로그인 로직)
                                .userInfoEndpoint(user ->
                                        user.userService(customOAuth2UserService))
                                // 2. 인증 성공 후 JWT 발급 & 쿠키/헤더 내려주기
                                .successHandler(userLoginSuccessHandler)
                                .tokenEndpoint(token ->
                                        token.accessTokenResponseClient(customClient)
                                )
                )
                // JWT 검증 필터 (모든 요청에서 AccessToken 확인)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000", "https://duckstar.kr"));
        cfg.setAllowCredentials(true);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
