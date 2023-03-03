# rabbit-hole-demo-application
This repository demo how to integrate your SpringBoot application with the ["Rabbit-Hole"](https://github.com/yonatankarp/rabbit-hole) library.

### Built With

* [OpenJDK Java 17](https://openjdk.org/projects/jdk/17/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Gradle](https://gradle.org/)

## Getting Started

To build this project locally follow the steps below:

### Prerequisites

* Java 11 or newer
* Gradle

### Installation

1. Clone the repo:
```shell
git clone git@github.com:yonatankarp/rabbit-hole-demo-application.git
```
2. Run Gradle's `build` command to fetch project dependencies:
```shell
gradle build
```
3. Run Gradle's `bootRun` command to start the service:
```shell
gradle bootRun
```

## Usage

This demo application have a single exchange called `myExchnage` with a single queue attached to it called `myQueue`.
The listener on the events of the queue can be found in the class `DemoListener`.

To send a message to the queue you can either use RabbitMQ dashboard and send the message over the exchange `myExchange`
with the routing key `my.routing.key` or use the service controller by sending the following command from your terminal:

```shell
curl -d "My message" -X POST http://localhost:8080/sendMessage
```

To test the retry mechanism go to the `DemoListener` and remove the comment of the RuntimeException bellow:
```java
//        throw new RuntimeException("Oh no! something bad happened :-(");
```


## Contributing

Contributions make the open-source community an amazing place to learn, inspire, and create. Any
contributions are **greatly appreciated** 🙏.

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
Link: [https://github.com/yonatankarp/rabbit-hole-demo-application](https://github.com/yonatankarp/rabbit-hole-demo-application)
