## Welcome to Rabbit Hole!

In this page you can find the explanations in great details about how to integrate with the "Rabbit-Hole" library and
how the library is working behind the sense.

## How to integrate with Rabbit-Hole

To integrate with the ["Rabbit Hole"](https://github.com/yonatankarp/rabbit-hole)
library you can either follow the steps bellow or check our [demo application](https://github.com/yonatankarp/rabbit-hole-demo-application).

### Steps

- Add the following to your `build.gradle` file in order to consume this library:
    - Make sure to set the following environment variables to consume the library:
        - `GITHUB_ACTOR` - your GitHub user account.
        - `GITHUB_PERSONAL_ACCESS_TOKEN` - the GitHub [access token](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token).

      ```groovy
      repositories {
          maven {
              name = "Rabbit Hole"
              url = uri("https://maven.pkg.github.com/yonatankarp/rabbit-hole")
              credentials {
                  username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
                  password = project.findProperty("gpr.key") ?: System.getenv('GITHUB_PERSONAL_ACCESS_TOKEN')
              }
          }
      }
      ```
- Add `maven` plugin to your gradle plugins:
    ```groovy
    plugins {
       id 'maven'
    }
    ```

- Add the library to your dependencies:
    ```groovy
    dependencies {
        implementation "com.yonatankarp:rabbit-hole:0.1.0"
    }
    ```

- The library configures all required beans for you by adding the `@SpringBootApplication` or
  `@SpringBootApplication` to your application as shown below.
    ```java
    @SpringBootApplication
    public class DemoApplication {
        public static void main(String[] args) {
            SpringApplication.run(DemoApplication.class, args);
        }
    }
    ```
    - You can generate also the beans yourself like this:
        ```java
        @Configuration
        public class DemoConfig {
            @Bean
            ContextUtils contextUtils(final GenericApplicationContext context) {
                return new ContextUtils(context);
            }
        
            @Bean
            QueueFactory queueFactory(
                    @Autowired final ContextUtils contextUtils,
                    @Autowired final ConnectionFactory connectionFactory) {
                return new QueueFactory(contextUtils, connectionFactory);
            }
        }
        ```

- Create your Rabbit queues by adding the configurations to you config file:
    - **NOTE** - The TTL of the exchange cannot be changed after it was set.  The only way you change the TTL is by deleting
      the current exchange and creating a new exchange.
  ```java
        @Autowired
        public void createRetryQueues(final QueueFactory factory) {
            final var config = Collections.singletonList(
                    new TopicQueueConfig("myQueue", "my.routing.key", 5000)
            );
            factory.createQueues("myExchange", config);
        }
    ```

- Add the following to your `application. properties`:
    ```properties
    # Required for RabbitMQ will acknowledge the messages going to the retry queue and won't return them to the top of the queue
    spring.rabbitmq.listener.simple.default-requeue-rejected=false
    ```

- Create a listener for your event. You can use the following template:
    ```java
    @Component
    public class EventListener {
        private static final int MAX_RETRIES = 5;
        
        @Qualifier("deadLetterRabbitTemplate")
        private RabbitTemplate rabbitTemplate;
        
        @RabbitListener(queues = { ... })
        public void process(Message message) {
            if (hasExceededRetryCount(message)) {
                sendMessageToDeadLetter(message);
                return;
            }
            // Consume your message
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
            this.rabbitTemplate.convertAndSend("testExchange.dead-letter", failedMessage);
        }
    }
    ```
