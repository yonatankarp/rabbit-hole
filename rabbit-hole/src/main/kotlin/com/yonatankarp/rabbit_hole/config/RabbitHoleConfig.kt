package com.yonatankarp.rabbit_hole.config

import com.yonatankarp.rabbit_hole.retry.QueueFactory
import com.yonatankarp.rabbit_hole.utils.ContextUtils
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.GenericApplicationContext

/**
 * The configuration of the library. This class will auto-configure the required beans into the
 * integrator application context for them to use.
 */
@Configuration
class RabbitHoleConfig {

    @Bean
    @ConditionalOnClass(GenericApplicationContext::class)
    fun contextUtils(@Autowired context: GenericApplicationContext) = ContextUtils(context)

    @Bean
    @ConditionalOnClass(ConnectionFactory::class)
    fun queueFactory(
        @Autowired contextUtils: ContextUtils,
        @Autowired connectionFactory: ConnectionFactory
    ) = QueueFactory(contextUtils, connectionFactory)
}
