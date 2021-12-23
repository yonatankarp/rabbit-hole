package com.yonatankarp.rabbit_hole

import com.yonatankarp.rabbit_hole.config.TestConfig
import com.yonatankarp.rabbit_hole.config.RabbitHoleConfig
import com.yonatankarp.rabbit_hole.retry.QueueFactory
import com.yonatankarp.rabbit_hole.utils.ContextUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RabbitHoleConfig::class, TestConfig::class])
class SpringbootRabbitmqRetryQueuesApplicationTests(
    @Autowired private val contextUtils: ContextUtils,
    @Autowired private val queueFactory: QueueFactory
) {
    @Test
    fun `should load context successfully`() {
    }

    @Test
    fun `should auto configure library successfully`() {
        Assertions.assertNotNull(contextUtils)
        Assertions.assertNotNull(queueFactory)
    }
}
