package com.yonatankarp.rabbit_hole.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ComponentTestConfig {
    @Bean
    ConnectionFactory testing() {
        return new CachingConnectionFactory();
    }
}
