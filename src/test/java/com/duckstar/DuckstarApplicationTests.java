package com.duckstar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
//@ActiveProfiles("test-memory")
@ActiveProfiles("test-db")
class DuckstarApplicationTests {

	@Test
	void contextLoads() {
	}
}
