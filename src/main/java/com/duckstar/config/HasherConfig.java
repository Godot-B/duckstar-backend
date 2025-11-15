package com.duckstar.config;

import com.duckstar.web.support.Hasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class HasherConfig {

    @Bean
    public Hasher hasher(@Value("${spring.security.ip-hash.key}") String key,
                         @Value("${spring.security.ip-hash.use-hex:false}") boolean useHex) {
        return new Hasher(key.getBytes(StandardCharsets.UTF_8), useHex);
    }
}
