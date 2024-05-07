package sample.camel.config;

import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.boot.CamelConfigurationProperties;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CamelConfigurationProperties.class)
public class CamelConfig {

    @Bean
    CamelContextConfiguration camelContextConfiguration() {
        return new CamelContextConfiguration() {
            @Override
            public void beforeApplicationStart(CamelContext context) {
                context.getShutdownStrategy().setTimeUnit(TimeUnit.SECONDS);
                context.getShutdownStrategy().setTimeout(5);
                context.getShutdownStrategy().setLogInflightExchangesOnTimeout(true);
                context.getShutdownStrategy().setSuppressLoggingOnTimeout(false);
            }

            @Override
            public void afterApplicationStart(CamelContext camelContext) {

            }
        };
    }
}
