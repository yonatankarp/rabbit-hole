package com.yonatankarp.rabbit_hole

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringbootRabbitmqRetryQueuesApplication

fun main(args: Array<String>) {
    runApplication<SpringbootRabbitmqRetryQueuesApplication>(*args)
}
