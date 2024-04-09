package com.chatup.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(properties = {"JWT_SECRET=secret"})
@TestPropertySource(properties = {
        "SPRING_SECURITY_USER_NAME=user",
        "SPRING_SECURITY_USER_PASSWORD=password",
})
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
