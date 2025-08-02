# Multi‑Tenant SaaS Application

This project demonstrates a multi‑tenant SaaS platform built with Spring Boot, PostgreSQL, Redis and [Rqueue](https://github.com/sonus21/rqueue) for asynchronous task processing.  It supports tenant sign‑up, JWT authentication, CRUD operations on contacts, soft deletion with audit logging, sharded databases and background tasks.  Database schemas are managed using Liquibase for both the central database and each shard.

## Prerequisites

* **Java 17** or higher
* **Maven** 3.8+
* **PostgreSQL** 13+ with databases created for the central schema and one or more shards.  By default the application expects databases named `central_db`, `shard1_db` and `shard2_db` on `localhost` with username/password `postgres`/`postgres`.  Adjust `src/main/resources/application.yml` as needed.
* **Redis** 6+.  An embedded Redis server is started automatically on port 6379 if none is available.  For production use, configure `spring.redis.host` and `spring.redis.port` to point to an external Redis instance.

## Bootstrapping the Application

1. **Initialize the databases**.  Ensure the central and shard databases exist.  Liquibase will create the required tables on first run:

   ```sh
   psql -U postgres -c 'CREATE DATABASE central_db;'
   psql -U postgres -c 'CREATE DATABASE shard1_db;'
   psql -U postgres -c 'CREATE DATABASE shard2_db;'
   ```

2. **Build the project**.  From the `saas-app` directory run:

   ```sh
   mvn clean package
   ```

   This compiles the application, runs the tests and packages an executable JAR in `target/`.

## Running the Application

The application can be run in different modes depending on your needs.

### Default Mode (Web + Worker)

This is the standard mode for development. It runs the web application and the Rqueue message workers in the same process.

```sh
java -jar target/saas-app-*.jar
```

### Web-Only Mode

This mode runs the web application without the Rqueue message workers. This is useful for running a dedicated web server instance.

```sh
java -jar target/saas-app-*.jar --spring.profiles.active=web
```

### Worker-Only Mode

This mode runs only the Rqueue message workers, without the web server. This is ideal for running dedicated, scalable worker instances.

```sh
java -jar target/saas-app-*.jar --spring.profiles.active=worker
```

### Worker for a Specific Queue

To run a worker that only processes specific queues, you can use the `rqueue.queues` property. For example, to run a worker that only processes the `testQueue`:

```sh
java -jar target/saas-app-*.jar --spring.profiles.active=worker --rqueue.queues=testQueue
```

You can also specify multiple queues as a comma-separated list:

```sh
java -jar target/saas-app-*.jar --spring.profiles.active=worker --rqueue.queues=testQueue,contactEventQueue
```

## API Endpoints

Swagger UI is available at [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) for detailed API documentation and testing.

## Rqueue Dashboard

The Rqueue dashboard is available at [http://localhost:8080/rqueue](http://localhost:8080/rqueue) for monitoring and managing message queues.
