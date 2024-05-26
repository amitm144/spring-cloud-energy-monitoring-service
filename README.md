# Energy Monitoring and Management Service

The Energy Monitoring and Management Service is a powerful and scalable solution for monitoring and optimizing energy consumption in a smart home environment. It collects data from various devices and appliances, provides insights into energy patterns, identifies areas of high consumption, and offers suggestions for reducing costs and environmental impact.

This service is a part of a larger course project where each team built a different service to create a complete smart home system. The Energy Monitoring and Management Service focuses specifically on the energy consumption aspect of the smart home ecosystem.

## Features

- Real-time monitoring of energy consumption from connected devices
- Generation of daily and monthly consumption reports and summaries
- Identification of high consumption devices and areas
- Alerts for over-consumption and over-current events
- Integration with Apache Kafka for event-driven communication

## Technologies Used

- Java
- Spring Boot
- Spring Data MongoDB
- Spring Cloud Stream
- Reactive Programming (Reactor)
- RSocket
- Apache Kafka
- MongoDB

## Architecture

The service follows a layered architecture for modularity and separation of concerns:

- Boundary Layer: Defines the data transfer objects (DTOs) for communication with external systems.
- Controller Layer: Handles incoming requests and orchestrates data flow between the boundary and logic layers.
- Logic Layer: Implements the core business logic, including energy consumption calculations and summary generation.
- Data Layer: Includes entities and repositories for storing and retrieving data from the MongoDB database.
- Service Layer: Provides integration with external services like Apache Kafka for messaging and queuing.
- Utility Layer: Contains helper classes and utilities for common functionalities.

## API Endpoints

- `GET /energy/summary`: Retrieve the live consumption summary.
- `GET /energy/summary/daily?date={date}`: Retrieve the daily consumption summary for a specific date.
- `GET /energy/summary/monthly?date={date}`: Retrieve the monthly consumption summary for a specific month.
- `GET /energy/warning/overcurrent`: Get all over-current warning events.
- `GET /energy/warning/consumption`: Get all over-consumption warning events.

This service seamlessly integrates with other services developed by different teams in the course project to provide a comprehensive smart home system. It leverages Apache Kafka for event-driven communication and data exchange with other services, ensuring a cohesive and efficient smart home ecosystem.
