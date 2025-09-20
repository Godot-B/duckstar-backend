//package com.duckstar.schedule;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@SpringBootTest
//@ActiveProfiles("test-db")
//public class ScheduleHandlerTest {
//    @Autowired
//    ScheduleHandler scheduleHandler;
//
//    @Test
//    @Transactional
//    public void scheduler() throws Exception {
//        long start = System.nanoTime();
//
//        //=== 테스트 코드 ===//
//
//        //given
//        LocalDateTime time = LocalDateTime.of(2025, 9, 21, 22, 0);
//
//        //when
//        scheduleHandler.runSchedule(time);
//
//
//        //then
//
//        long end = System.nanoTime();
//        double elapsedSeconds = (end - start) / 1_000_000_000.0;
//        System.out.println("⏰ 실행 시간: " + elapsedSeconds + " seconds");
//    }
//}
