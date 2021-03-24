<!--
This README is based on "Best-README-Template" for more info
visit https://github.com/othneildrew/Best-README-Template#about-the-project
-->

<h3 align="center">Rabbit Hole</h3>
<p align="center">
	The "Rabbit Hole" project is a JVM library that allows you to easily create retry mechanisms in
	RabbitMQ brokers without blocking the queue. The implementation is based on the article
	<a href="https://programmerfriend.com/rabbit-mq-retry/">Implementing Retries using RabbitMQ and Spring Boot 2</a>.
	<br />
	<br />
	<a href="https://github.com/yonatankarp/rabbit-hole"><strong>Explore the docs »</strong></a>
	<br />
	<br />
	<a href="https://github.com/yonatankarp/rabbit-hole/issues">Report Bug</a>
	·
	<a href="https://github.com/yonatankarp/rabbit-hole/issues">Request Feature</a>
</p>


![Build](https://github.com/yonatankarp/rabbit-hole/actions/workflows/build.yml/badge.svg)
![Code Style](https://github.com/yonatankarp/rabbit-hole/actions/workflows/code_style.yml/badge.svg)
[![codecov](https://codecov.io/gh/yonatankarp/rabbit-hole/branch/main/graph/badge.svg?token=BZ118ARLZQ)](https://codecov.io/gh/yonatankarp/rabbit-hole)
[![Known Vulnerabilities](https://snyk.io/test/github/yonatankarp/rabbit-hole/badge.svg)](https://snyk.io/test/github)
![GitHub last commit](https://img.shields.io/github/last-commit/yonatankarp/rabbit-hole)
![GitHub issues](https://img.shields.io/github/issues-raw/yonatankarp/rabbit-hole)
![GitHub top language](https://img.shields.io/github/languages/top/yonatankarp/rabbit-hole)
![Maintenance](https://img.shields.io/maintenance/yes/2021)
![GitHub](https://img.shields.io/github/license/yonatankarp/rabbit-hole)


<!-- TABLE OF CONTENTS -->
<details open="open">
<summary><h2 style="display: inline-block">Table of Contents</h2></summary>
<ol>
	<li>
	<a href="#about-the-project">About The Project</a>
	<ul>
		<li><a href="#built-with">Built With</a></li>
	</ul>
	</li>
	<li>
	<a href="#getting-started">Getting Started</a>
	<ul>
		<li><a href="#prerequisites">Prerequisites</a></li>
		<li><a href="#installation">Installation</a></li>
	</ul>
	</li>
	<li><a href="#usage">Usage</a></li>
	<li><a href="#roadmap">Roadmap</a></li>
	<li><a href="#contributing">Contributing</a></li>
	<li><a href="#license">License</a></li>
	<li><a href="#contact">Contact</a></li>
</ol>
</details>



<!-- ABOUT THE PROJECT -->

## About The Project

### Problem  statement

When a message fails in the `spring-boot-starter-amqp` library which is configured with an exponential backoff
mechanism will return the message to the top of the queue and block the queue until the message is consumed.

### Solution

This library introduces a retry mechanism that will not block the main queue based on the article
["Implementing Retries using RabbitMQ and Spring Boot 2"](https://programmerfriend.com/rabbit-mq-retry/"), while the
main change from the article solution is that this library generalizes the solution into `N` queues instead of just `1`.
### Built With

* [OpenJDK Java 11](https://openjdk.java.net/projects/jdk/11/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Gradle](https://gradle.org/)

## Getting Started

To build this library locally follow those steps.

### Prerequisites

* Java 11 or newer
* Gradle

### Installation

1. Clone the repo
```shell
git clone https://github.com/yonatankarp/rabbit-hole.git
```
2. Run Gradle build command to fetch all the project  dependencies
```shell
gradle build
```

3. Publish the library to your local Maven repository
```shell
gradle publishToMavenLocal
```

## Usage

<!-- TODO: amend documentation according to current implementation! -->

1. To use this library add the following to your `build.gradle` file

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

2. To consume the library set the following environment variables:

- `GITHUB_ACTOR` - your GitHub user account.
- `GITHUB_PERSONAL_ACCESS_TOKEN` - the GitHub [access token](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token).

3. Add the library to your dependencies:
```groovy
dependencies {
    implementation "com.yonatankarp:rabbit-hole"
}
```

4. The library will configure all required beans for you by adding the `@SpringBootApplication` or
   `@SpringBootApplication` to your application as shown bellow.
```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

If you prefer, you can generate the beans yourself using the following code:
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
5. Now you can create the queues by adding the configurations to you config file.

```java
    @Autowired
    public void createRetryQueues(final QueueFactory factory) {
        final var config = Collections.singletonList(
                new TopicQueueConfig("myQueue", "my.routing.key", 5000)
        );
        factory.createQueues("myExchange", config);
    }
```

**NOTE** - The TTL of the exchange cannot be changed after it was set.  The only way you change the  TTL is to  delete
the current exchange and create a new one.

6. Add the following to your `application. properties`
```properties
# Required for RabbitMQ will acknowledge the messages going to the retry queue and won't return them to the top of the queue
spring.rabbitmq.listener.simple.default-requeue-rejected=false
```

7. Create a listener for your event. You can use the following template:
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

<!-- TODO: add link to full configuration README once created -->

## Roadmap

See the [open issues](https://github.com/yonatankarp/rabbit-hole/issues) for a list of proposed
features (and known issues).


## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any
contributions you make are **greatly appreciated** 🙏.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request to the `main` branch


## License

Distributed under the MIT License. See `LICENSE` for more information.


## Contact

Yonatan Karp-Rudin - [@yonvata](https://twitter.com/yonvata) - yonvata@gmail.com

Project
Link: [https://github.com/yonatankarp/rabbit-hole](https://github.com/yonatankarp/rabbit-hole)
