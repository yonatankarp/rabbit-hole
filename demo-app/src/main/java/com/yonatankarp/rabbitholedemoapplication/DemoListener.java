package com.yonatankarp.rabbitholedemoapplication;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DemoListener {
    private static final int MAX_RETRIES = 1;

    // We're suppressing the warning as the bean coming from rabbit-hole when the context is loaded
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    @Qualifier("deadLetterRabbitTemplate")
    private RabbitTemplate rabbitTemplate;


    @RabbitListener(queues = DemoConfig.QUEUE_NAME)
    public void process(Message message) {
        if (hasExceededRetryCount(message)) {
            sendMessageToDeadLetter(message);
            return;
        }

        System.out.println(new String(message.getBody()));

        // To test the retry mechanism simply remove the comment from the line bellow
//        throw new RuntimeException("Oh no! something bad happened :-(");
    }

    private boolean hasExceededRetryCount(final Message message) {
        var xDeathHeader = message.getMessageProperties().getXDeathHeader();
        if (xDeathHeader != null && xDeathHeader.size() >= 1) {
            final Long count = (Long) xDeathHeader.get(0).get("count");
            return count >= MAX_RETRIES;
        }

        return false;
    }

    private void sendMessageToDeadLetter(final Message failedMessage) {
        this.rabbitTemplate.convertAndSend("testQueue.dead-letter", failedMessage);
    }
}
