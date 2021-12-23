package com.yonatankarp.rabbit_hole.demo_app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RabbitHoleDemoApplication

fun main(args: Array<String>) {
    runApplication<RabbitHoleDemoApplication>(*args)
}
