package il.ac.afeka.energyservice.configurations;

import il.ac.afeka.energyservice.utils.MessageBoundaryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class MessageBoundaryFactoryConfig {
        @Bean
        public MessageBoundaryFactory messageBoundaryFactory() {
            return MessageBoundaryFactory.get();
        }
}

