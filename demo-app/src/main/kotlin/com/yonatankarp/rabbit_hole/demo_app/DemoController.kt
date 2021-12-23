package com.yonatankarp.rabbit_hole.demo_app

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import com.yonatankarp.rabbit_hole.demo_app.DemoConfig.Companion.EXCHANGE_NAME
import com.yonatankarp.rabbit_hole.demo_app.DemoConfig.Companion.ROUTING_KEY

@RestController
class DemoController(private val rabbitTemplate: RabbitTemplate) {
    @PostMapping(value = ["/sendMessage"])
    fun sendMessage(@RequestBody message: String) =
        rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message)
}
