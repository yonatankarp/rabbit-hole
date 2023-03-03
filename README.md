<!--
This README is based on "Best-README-Template" for more info
visit https://github.com/othneildrew/Best-README-Template#about-the-project
-->

<h3 align="center">Rabbit Hole</h3>
<p align="center">
	"Rabbit Hole" allows you to easily create retry mechanisms using any JVM-based language against
	RabbitMQ brokers, but without blocking the queue. The implementation is based on the article
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


![Build](https://github.com/yonatankarp/rabbit-hole/actions/workflows/ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/yonatankarp/rabbit-hole/branch/main/graph/badge.svg?token=BZ118ARLZQ)](https://codecov.io/gh/yonatankarp/rabbit-hole)
[![Known Vulnerabilities](https://snyk.io/test/github/yonatankarp/rabbit-hole/badge.svg)](https://snyk.io/test/github)
![GitHub last commit](https://img.shields.io/github/last-commit/yonatankarp/rabbit-hole)
![GitHub issues](https://img.shields.io/github/issues-raw/yonatankarp/rabbit-hole)
![GitHub top language](https://img.shields.io/github/languages/top/yonatankarp/rabbit-hole)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_rabbit-hole&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=yonatankarp_rabbit-hole)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_rabbit-hole&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_rabbit-hole)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_rabbit-hole&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=yonatankarp_rabbit-hole)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_rabbit-hole&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=yonatankarp_rabbit-hole)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=yonatankarp_rabbit-hole&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=yonatankarp_rabbit-hole)
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

### Background

When a message fails in the `spring-boot-starter-amqp` library, and is configured with an exponential backoff
mechanism, Rabbit will return the message to the top of the queue and block the queue until the message is consumed.

### Solution

This library introduces a retry mechanism that will not block the main queue, referencing prior art in
["Implementing Retries using RabbitMQ and Spring Boot 2"](https://programmerfriend.com/rabbit-mq-retry/").

The major difference between the solution linked above is that this library is that here we generalize the solution using `N` queues instead of a single queue`.

### Built With

* [OpenJDK Java 17](https://openjdk.org/projects/jdk/17/)
* [Spring Boot](https://spring.io/projects/spring-boot)
* [Gradle](https://gradle.org/)

## Getting Started

To build this library locally follow the steps below:

### Prerequisites

* Java 17 or newer
* Gradle

### Installation

1. Clone the repo:
```shell
git clone https://github.com/yonatankarp/rabbit-hole.git
```
2. Run Gradle's `build` command to fetch project dependencies:
```shell
gradle build
```

3. Publish the library to your local Maven repository:
```shell
gradle publishToMavenLocal
```

## Usage

To see how to use the library check our latest integration documentation [here](./demo-app/README.md).

## Roadmap

See the [open issues](https://github.com/yonatankarp/rabbit-hole/issues) for a list of proposed
features (and known issues).


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
Link: [https://github.com/yonatankarp/rabbit-hole](https://github.com/yonatankarp/rabbit-hole)
