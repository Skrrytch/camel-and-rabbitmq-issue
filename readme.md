## Example project to demonstrate different behaviour of camel-rabbitmq and cammel-spring-rabbitmq

This example project is intended to show the different behaviour of the components

- `camel-rabbitmq` (deprecated, removed in camel 4.x)
- `camel-sprint-rabbitmq`

###  Configuring RabbitMQ

The sample application uses `localhost:5672` to connect to the RabbitMQ broker.
This can be configured in the `application.properties` file.

The login information is the default `guest/guest` account, which can be configured
in the `application.properties` file as well.

### How to run

The sample requires a RabbitMQ broker to be running.

You can for example easily start RabbitMQ via Docker

    docker run -it -p 5672:5672 --hostname my-rabbit --name some-rabbit rabbitmq:3

Then you can run this example using

    mvn spring-boot:run

### Description of my issue

This example project is intended to show the different behaviour of the components
`camel-rabbitmq` (deprecated, removed in camel 4.x) and `camel-sprint-rabbitmq`:

There are two route builders:

1. `CamelRabbitRouteBuilder`, uses Camel component `camel-rabbitmq`
2. `CamelSpringRabbitRouteBuilder`, uses Camel component `camel-spring-rabbitmq`

Both route builders define two routes:

- The first route sends a text message to a RabbitMQ exchange
- The other route receives that message from RabbitMQ and simulates an error (exception).
- The exception handling defines a retry every 3 seconds for this test.

### Test procedure

Run the application, so you can see an output of the retry for both, like this:

```
SpringRabbit REDELIVERY: Message from 2024-05-07T14:43:41.954489120+02:00[Europe/Berlin]
Camel-Rabbit REDELIVERY: Message from 2024-05-07T14:41:33.463028488+02:00[Europe/Berlin]
SpringRabbit REDELIVERY: Message from 2024-05-07T14:43:41.954489120+02:00[Europe/Berlin]
Camel-Rabbit REDELIVERY: Message from 2024-05-07T14:41:33.463028488+02:00[Europe/Berlin]
...
```

Use `jconsole` (or similar) to call the Camel JMX routine `stop()` on both receive routes
while they perform a retry.

### Observation

1. With 'camel-rabbit' the route is terminated after a timeout,
   and the retry is interrupted (java.lang.InterruptedException: sleep interrupted),
   and the message is back in the RabbitMQ ready for the next delivery (when the route is restarted)
2. With 'camel-spring-rabbitmq' the route is also terminated after a timeout,
   but the retry continues.

### Our scenario in a more complex project

We absolutely need a way to interrupt a retry in progress by stopping the route.

Use cases:
- maintenance
- possibility to stop the retry, remove the 
message from the queue and then restart.

