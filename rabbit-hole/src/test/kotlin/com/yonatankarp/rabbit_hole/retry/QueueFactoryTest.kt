package com.yonatankarp.rabbit_hole.retry

import com.yonatankarp.rabbit_hole.exceptions.ExchangeException
import com.yonatankarp.rabbit_hole.exceptions.QueueConfigException
import com.yonatankarp.rabbit_hole.retry.topic.TopicQueueConfig
import com.yonatankarp.rabbit_hole.utils.ContextUtils
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import java.util.stream.Stream

class QueueFactoryTest {
    private val contextUtils: ContextUtils = mock(ContextUtils::class.java)
    private val connectionFactory: ConnectionFactory = mock(ConnectionFactory::class.java)
    private lateinit var factory: QueueFactory

    @BeforeEach
    fun init() {
        reset(contextUtils)
        reset(connectionFactory)
        factory = QueueFactory(contextUtils, connectionFactory)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class InvalidExchangeNameTest {
        @ParameterizedTest
        @MethodSource("blankStrings")
        fun `should throw an error for invalid exchange name`(exchangeName: String?) {
            // Given null exchange name and valid queue config
            val configs = listOf(TopicQueueConfig("testQueue", "testRoutingKey", 1000))

            // When we call the factory with invalid exchange name
            // Then we expect exception to be thrown
            assertThrows(ExchangeException::class.java) { factory.createQueues(exchangeName, configs) }
        }

        private fun blankStrings(): Stream<String?> = Stream.of("", "   ", null)
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class QueueConfigTest {
        @ParameterizedTest
        @MethodSource("queueConfigs")
        fun `should throw an error for invalid queue config`(case: TestCase) {
            // Given invalid queue configuration
            // When we call the factory
            // Then we expect exception to be thrown
            assertThrows(case.exception) { factory.createQueues("testExchange", case.config) }
        }

        inner class TestCase(val config: List<QueueConfig>?, val exception: Class<out Throwable>) {
            override fun toString() = "config: $config, exception: $exception"
        }
        inner class DummyQueueConfig : QueueConfig

        private fun queueConfigs(): Stream<TestCase> = Stream.of(
            TestCase(null, QueueConfigException::class.java),
            TestCase(listOf(
                TopicQueueConfig("testQueue", "testRoutingKey", 1000),
                DummyQueueConfig()
            ), QueueConfigException::class.java),
            TestCase(listOf(DummyQueueConfig()), IllegalArgumentException::class.java)
        )
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class TopicConfigTest {
        @Test
        fun testTopicConfiguration() {
            // Given a topic queue configuration
            val configs = listOf(TopicQueueConfig("testQueue", "testRoutingKey", 1000))

            // When we call the factory
            factory.createQueues("testExchange", configs)

            // Then we expect the context utilities class to be called with the main topic exchange
            // and the retry topic exchange
            Mockito.verify(contextUtils, Mockito.times(1))
                .registerTopicExchange(ArgumentMatchers.any())
        }
    }
}


