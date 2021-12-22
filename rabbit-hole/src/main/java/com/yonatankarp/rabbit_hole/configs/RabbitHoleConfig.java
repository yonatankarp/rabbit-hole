package com.yonatankarp.rabbit_hole.configs;

import com.yonatankarp.rabbit_hole.retry.QueueFactory;
import com.yonatankarp.rabbit_hole.utils.ContextUtils;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

/**
* The configuration of the library. This class will auto-configure the required beans into the
* integrator application context for them to use.
*/
@Configuration
public class RabbitHoleConfig {

    @Bean
    @ConditionalOnClass(GenericApplicationContext.class)
    ContextUtils contextUtils(final GenericApplicationContext context) {
        return new ContextUtils(context);
    }

    @Bean
    @ConditionalOnClass(ConnectionFactory.class)
    QueueFactory queueFactory(
            @Autowired final ContextUtils contextUtils,
            @Autowired final ConnectionFactory connectionFactory) {
        return new QueueFactory(contextUtils, connectionFactory);
    }
}
