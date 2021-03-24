package com.yonatankarp.rabbit_hole;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.yonatankarp.rabbit_hole.retry.QueueFactory;
import com.yonatankarp.rabbit_hole.utils.ContextUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootRabbitmqRetryQueuesApplicationTests {

    @Autowired private ContextUtils contextUtils;
    @Autowired private QueueFactory queueFactory;

    @Test
    void contextLoads() {}

    @Test
    void testAutoConfiguration() {
        assertNotNull(contextUtils);
        assertNotNull(queueFactory);
    }
}
