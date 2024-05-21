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
import java.util.concurrent.RejectedExecutionException;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.rabbitmq.RabbitMQConstants;
import org.apache.camel.component.springrabbit.SpringRabbitMQConstants;
import org.apache.camel.processor.errorhandler.RedeliveryPolicy;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

/**
 * A simple Camel route that triggers from a timer and routes to RabbitMQ
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class CamelSpringRabbitRouteBuilder extends RouteBuilder {

    @Override
    public void configure() {

        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy()
                .maximumRedeliveries(999)
                .redeliveryDelay(3000)
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .allowRedeliveryWhileStopping(false);

        getCamelContext().getRegistry().bind("myRedeliveryPolicy", redeliveryPolicy);

        onException(IllegalArgumentException.class)
                .redeliveryPolicyRef("myRedeliveryPolicy")
                .process(x -> System.out.println(ZonedDateTime.now() + ": SpringRabbit FAILURE: " + x.getMessage().getBody()))
                .onRedelivery(x -> System.out.println(ZonedDateTime.now() + ": SpringRabbit REDELIVERY: " + x.getMessage().getBody()));

        from("timer:spring-rabbit-timer?delay=1000&period=0")
                .id("spring-rabbit-timer")
                .transform(simple("Message from " + ZonedDateTime.now()))
                .to("spring-rabbitmq:spring-rabbit-ex?acknowledgeMode=NONE");

        // Use RedeliveryPolicy to set allowRedeliveryWhileStopping=false
        // acknowledgeMode = NONE: RabbitMQ doens't wait for ACK
        // acknowledgeMode = MANUAL: We have to send ACK manually
        // acknowledgeMode = AUTO: Camel sends ACK automatically

        from("spring-rabbitmq:spring-rabbit-ex?queues=springrabbit&autoDeclare=true&rejectAndDontRequeue=false&acknowledgeMode=MANUAL")
                .id("spring-rabbit-receiver")
                .log("SpringRabbit received: ${body}")
                .process(x -> {
                    if (Math.random() < 0.9) {
                        throw new IllegalArgumentException("Spring-Rabbit: We simulate an error for message '" + x.getMessage().getBody(String.class) + "'");
                    } else {
                        System.out.println("MESSAGE PROCESSED SUCCESSFULLY");
                        Channel channel = x.getProperty(SpringRabbitMQConstants.CHANNEL, Channel.class);
                        long deliveryTag = x.getMessage().getHeader(SpringRabbitMQConstants.DELIVERY_TAG, Long.class);
                        channel.basicAck(deliveryTag, false);

                    }
                });
    }


}
