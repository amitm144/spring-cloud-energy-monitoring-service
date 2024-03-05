package il.ac.afeka.rsocketmessagingservice.kafkaMessagesListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.logic.EnergyConsumptionsService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.function.Consumer;

@Configuration
public class MessageListener {
	private ObjectMapper jackson;
	private EnergyConsumptionsService energyService;
	private Log logger = LogFactory.getLog(MessageListener.class);

	public MessageListener(EnergyConsumptionsService energyService) {
		this.energyService = energyService;
	}

	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}

	@Bean
	public Consumer<String> messageSink(){
		return stringInput->{
			try {
				this.logger.trace("*** received: " + stringInput);

				MessageBoundary message = this.jackson.readValue(stringInput, MessageBoundary.class);

				if (message.getMessageDetails() == null) {
					message.setMessageDetails(new HashMap<>());
				}
				message.getMessageDetails()
						.put("status", "received-from-kafka");

				MessageBoundary storedMessage = this.energyService
						.store(message)
						.block();

				this.logger.info("*** stored: " + storedMessage);
			}catch (Exception e) {
				e.printStackTrace();
				this.logger.error(e);
			}
		};
	}
}
