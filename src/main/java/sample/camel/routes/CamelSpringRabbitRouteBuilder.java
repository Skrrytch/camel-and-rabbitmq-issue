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
import org.springframework.stereotype.Component;

/**
 * A simple Camel route that triggers from a timer and routes to RabbitMQ
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class CamelSpringRabbitRouteBuilder extends RouteBuilder {

    @Override
    public void configure() {

        onException(IllegalArgumentException.class)
                .maximumRedeliveries(999) // to have enough time for testing
                .redeliveryDelay(3000) // every 3 seconds
                .process(x -> System.out.println(ZonedDateTime.now() + ": SpringRabbit FAILURE: " + x.getMessage().getBody()))
                .onRedelivery(x -> System.out.println(ZonedDateTime.now() + ": SpringRabbit REDELIVERY: " + x.getMessage().getBody()));

        from("timer:spring-rabbit-timer?delay=1000&period=0")
                .id("spring-rabbit-timer")
                .transform(simple("Message from " + ZonedDateTime.now()))
                .to("spring-rabbitmq:spring-rabbit-ex");

        from("spring-rabbitmq:spring-rabbit-ex?queues=springrabbit&autoDeclare=true")
                .id("spring-rabbit-receiver")
                .log("SpringRabbit received: ${body}")
                .process(x -> {
                    throw new IllegalArgumentException("Spring-Rabbit: We simulate an error for message '" + x.getMessage().getBody(String.class) + "'");
                });
    }


}
