# Hotel Introvert — Distributed Hotel Simulation

A university project developed as part of the **Concurrent Programming** course.

The project simulates the operation of a small hotel using asynchronous messaging and concurrent event generators.

It includes **three implementations of the same distributed system**, demonstrating different messaging technologies and cross-language communication.

## Technologies

- **Java 17**
- **Python 3**
- **RabbitMQ**
- **Apache Kafka**
- **Apache Maven**
- **Docker Compose**
- **JSON**
- **Multithreading**

## Implementations

| Implementation | Producers | Consumers | Message Broker |
|---|---|---|---|
| RabbitMQ — Java | Java | Java | RabbitMQ |
| Kafka — Java | Java | Java | Apache Kafka |
| RabbitMQ — Python + Java | Python | Java | RabbitMQ |

### 1. Java + RabbitMQ

A Java-based implementation using RabbitMQ for asynchronous communication between event generators and the hotel processing components.

The application generates check-in, check-out, and meal events and processes them using a shared hotel database and billing logic.

**Directory:** `rabbitmq-java/`

### 2. Java + Apache Kafka

An alternative implementation of the same hotel simulation using Apache Kafka as the messaging platform.

The project includes a Docker Compose configuration for the Kafka environment.

**Directory:** `kafka-java/`

### 3. Python + Java + RabbitMQ

A cross-language implementation demonstrating communication between applications written in different programming languages.

- **Python** generates hotel events and publishes JSON messages to RabbitMQ.
- **Java** consumes the messages, manages hotel state, and handles billing.

This implementation demonstrates interoperability through a shared messaging protocol.

**Directory:** `rabbitmq-python-java/`

## Simulation Rules

The simulated hotel has **10 single rooms**, numbered from 0 to 9.

Time is accelerated so that **one real second represents one simulated hour**.

The simulation includes:

- **Two check-in generators** producing new guest arrival events.
- **Two check-out generators** producing departure events according to a configurable probability.
- **One meal generator** producing breakfast, lunch, and dinner events.
- **Room management** tracking occupied and available rooms.
- **Billing** calculating charges when a guest checks out.

### Meals and Pricing

Meals are scheduled at specific simulated hours:

| Meal | Time |
|---|---|
| Breakfast | 07:00 |
| Lunch | 12:00 |
| Dinner | 18:00 |

The hotel uses the following pricing model:

| Service | Price |
|---|---|
| Overnight stay | €10 |
| Breakfast | €10 |
| Lunch | €10 |
| Dinner | €10 |

An overnight stay is counted when the guest occupies a room during the night period between 22:00 and 06:00.

## Project Structure

```text
hotel-introvert-distributed-system/
├── rabbitmq-java/
│   ├── pom.xml
│   └── src/
│
├── kafka-java/
│   ├── pom.xml
│   ├── docker-compose.yml
│   └── src/
│
├── rabbitmq-python-java/
│   ├── python-producers/
│   │   └── generators.py
│   └── java-consumer/
│       ├── pom.xml
│       └── src/
│
└── README.md
```

Each Java implementation is maintained as a separate Maven project.

## Getting Started

### Prerequisites

Depending on the implementation, you will need:

- JDK 17
- Apache Maven
- RabbitMQ Server
- Apache Kafka and Docker Compose (for the Kafka implementation)
- Python 3 and the `pika` package (for Python producers)

### Java + RabbitMQ

1. Start a local RabbitMQ server.
2. Open `rabbitmq-java/` as a Maven project in IntelliJ IDEA.
3. Resolve Maven dependencies.
4. Run the `HotelSimulation` main class.

### Java + Kafka

1. Open the `kafka-java/` directory.
2. Start the Kafka infrastructure using the provided Docker Compose configuration:

   ```bash
   docker compose up -d
   ```

3. Resolve Maven dependencies.
4. Run the `HotelSimulation` main class.

### Python + Java + RabbitMQ

1. Start a local RabbitMQ server.
2. Open `rabbitmq-python-java/java-consumer/` in IntelliJ IDEA.
3. Run the Java `HotelConsumerApp` class.
4. Install the Python RabbitMQ client:

   ```bash
   pip install pika
   ```

5. Start the Python producers:

   ```bash
   python generators.py
   ```

6. Enter the desired check-out probability when prompted.

The Python producers publish JSON events to RabbitMQ, and the Java consumer processes them.

## Concepts Demonstrated

- Distributed application architecture
- Message-oriented middleware
- Producer–consumer communication
- Asynchronous event processing
- Multithreading and synchronization
- Shared application state
- JSON message serialization
- Cross-language interoperability
- Simulation of concurrent real-world events

## Notes

This project was developed for educational purposes. It demonstrates distributed programming concepts rather than a production-ready hotel management system.

The three implementations are intended to illustrate alternative approaches to messaging and application integration.
