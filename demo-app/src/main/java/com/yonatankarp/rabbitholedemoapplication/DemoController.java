package com.yonatankarp.rabbitholedemoapplication;

import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class DemoController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping(value = "/sendMessage")
    public void sendMessage(@RequestBody final String message) {
        rabbitTemplate.convertAndSend(DemoConfig.EXCHANGE_NAME, DemoConfig.ROUTING_KEY, message);
    }
}
