/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sample.camel.routes;

import java.time.ZonedDateTime;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and routes to RabbitMQ
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class CamelRabbitRouteBuilder extends RouteBuilder {

    @Value("${spring.rabbitmq.host}")
    String host;

    @Value("${spring.rabbitmq.port}")
    String port;

    @Value("${spring.rabbitmq.username}")
    String username;

    @Value("${spring.rabbitmq.password}")
    String password;

    @Value("${spring.rabbitmq.virtual-host}")
    String vhost;

    @Override
    public void configure() {

        onException(IllegalArgumentException.class)
                .maximumRedeliveries(999) // to have enough time for testing
                .redeliveryDelay(3000) // every 3 seconds
                .process(x -> System.out.println(ZonedDateTime.now() + ": Camel-Rabbit FAILURE: " + x.getMessage().getBody(String.class)))
                .onRedelivery(x -> System.out.println(ZonedDateTime.now() + ": Camel-Rabbit REDELIVERY: " + x.getMessage().getBody(String.class)));

        // Brute force (for this test)
        String rabbitConnect = "&hostname=%s&portNumber=%s&vhost=%s&username=%s&password=%s"
                .formatted(host, port, vhost, username, password);

        from("timer:camel-rabbit-timer?delay=1000&period=0")
                .id("camel-rabbit-timer")
                .transform(simple("Message from " + ZonedDateTime.now()))
                .to("rabbitmq:camel-rabbit-ex?queue=camelrabbit&autoDelete=false" + rabbitConnect);

        from("rabbitmq:camel-rabbit-ex?queue=camelrabbit&autoDelete=false&autoAck=false" + rabbitConnect)
                .id("camel-rabbit-receiver")
                .log("Camel-Rabbit received: ${body}")
                .process(x -> {
                    throw new IllegalArgumentException("CamelRabbit: We simulate an error for message '" + x.getMessage().getBody(String.class) + "'");
                });
    }


}
