package com.yonatankarp.rabbit_hole.demo_app

import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component


@Component
class DemoListener(
    // We're suppressing the warning as the bean coming from rabbit-hole when the context is loaded
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired @Qualifier("deadLetterRabbitTemplate")
    private var rabbitTemplate: RabbitTemplate
) {

    @RabbitListener(queues = [DemoConfig.QUEUE_NAME])
    fun process(message: Message) {
        if (message.hasExceededRetryCount()) {
            message.sendMessageToDeadLetter()
            return
        }
        println(String(message.body))

        /** To test the retry mechanism simply remove the comment from the line bellow **/
//        throw new RuntimeException("Oh no! something bad happened :-(");
    }

    private fun Message.hasExceededRetryCount(): Boolean {
        val xDeathHeader = messageProperties.xDeathHeader
        if (xDeathHeader != null && xDeathHeader.size >= 1) {
            val count = xDeathHeader.first()["count"] as Long
            return count >= MAX_RETRIES
        }
        return false
    }

    private fun Message.sendMessageToDeadLetter() =
        rabbitTemplate.convertAndSend("testQueue.dead-letter", this)

    companion object {
        private const val MAX_RETRIES = 1
    }
}
