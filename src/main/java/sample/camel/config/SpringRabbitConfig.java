package sample.camel.config;

import org.apache.camel.spring.boot.CamelConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CamelConfigurationProperties.class)
public class SpringRabbitConfig {

//    @Bean
//    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
//        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
//        factory.setConnectionFactory(connectionFactory);
//        factory.setForceStop(true); // Makes no difference
//        factory.setConcurrentConsumers(1);
//        return factory;
//    }
}
