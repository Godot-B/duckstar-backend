package com.duckstar.service;

import com.duckstar.domain.Member;
import com.duckstar.security.repository.MemberRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Disabled("로컬 개발용 테스트")
@ActiveProfiles("test-db")
public class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
//    @Rollback(false)
    public void testMember() {
        Member member = memberRepository.findById(2L).get();
        member.withdraw();
    }
}
