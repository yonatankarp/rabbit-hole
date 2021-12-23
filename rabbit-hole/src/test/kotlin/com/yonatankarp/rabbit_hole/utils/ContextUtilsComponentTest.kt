package com.yonatankarp.rabbit_hole.utils

import com.yonatankarp.rabbit_hole.config.TestConfig
import com.yonatankarp.rabbit_hole.configs.RabbitHoleConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RabbitHoleConfig::class, TestConfig::class])
class ContextUtilsComponentTest(
    @Autowired private val context: ApplicationContext,
    @Autowired private val contextUtils: ContextUtils,
    @Autowired private val connectionFactory: ConnectionFactory
) {
    @Test
    fun `should register queue correctly`() {
        // Given a queue
        val queue = Queue("testQueue")

        // When we call registry method
        contextUtils.registerQueue(queue)

        // Then we expect the queue to be available in the context
        val actual = context.getBean("testQueue", Queue::class.java)
        assertNotNull(actual)
        assertEquals(queue.name, actual.name)
    }

    @Test
    fun `should register topic exchange successfully`() {
        // Given a topic exchange
        val exchange = TopicExchange("testExchange")

        // When we call registry method
        contextUtils.registerTopicExchange(exchange)

        // Then we expect the correct exchange to be available in the context
        val actual = context.getBean("testExchange", TopicExchange::class.java)
        assertNotNull(actual)
        assertEquals(exchange.name, actual.name)
        assertEquals(exchange.type, actual.type)
    }

    @Test
    fun `should register binding successfully`() {
        // Given a valid exchange, queue and binding
        val queue = Queue("test")
        val exchange = TopicExchange("testExchange")
        val routingKey = "routing-key"
        val binding = BindingBuilder.bind(queue).to(exchange).with(routingKey)

        // When we call registry method
        contextUtils.registerBinding("testBinding", binding)

        // Then we expect the queue to be available in the context
        val actual = context.getBean("testBinding", Binding::class.java)
        assertNotNull(actual)
        assertEquals(binding.exchange, exchange.name)
        assertEquals(binding.routingKey, routingKey)
    }

    @Test
    fun `should register rabbit template successfully`() {
        // Given a rabbit template
        val rabbitTemplate = RabbitTemplate(connectionFactory)

        // When we call registry method
        contextUtils.registerRabbitTemplate("myRabbitTemplate", rabbitTemplate)

        // Then we expect the queue to be available in the context
        val actual = context.getBean("myRabbitTemplate", RabbitTemplate::class.java)
        assertNotNull(actual)
        assertEquals(rabbitTemplate, actual)
    }
}
