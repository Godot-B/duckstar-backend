package com.duckstar.config;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
public class QueryDSLConfig {
    private final EntityManager entityManager;

    /**
     * 'java.lang.Object org.hibernate.ScrollableResults.get(int)' 오류
     * : groupBy() 메서드를 사용하여 쿼리를 작성할 때 orderBy() 메서드와 함께 사용할 때 발생하는 문제.
     *      - JPQLTemplates.DEFAULT 인자 넣어서 해결
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
    }
}