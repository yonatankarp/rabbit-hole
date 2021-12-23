package com.yonatankarp.rabbit_hole.config

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {
    @Bean
    fun connectionFactory() = CachingConnectionFactory()
}
