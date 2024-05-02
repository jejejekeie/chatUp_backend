package com.chatup.backend;

import com.chatup.backend.config.WebSocketConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(WebSocketConfig.class)
@TestPropertySource
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
