package dev.tyrxuan.middleware.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class ApiTest {

    @Test
    public void contextLoad() {
        log.info("context load...");
    }
}
