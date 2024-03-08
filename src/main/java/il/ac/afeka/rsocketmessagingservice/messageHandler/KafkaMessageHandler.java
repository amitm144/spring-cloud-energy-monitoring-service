package il.ac.afeka.rsocketmessagingservice.messageHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.logic.EnergyConsumptionService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class KafkaMessageHandler {
	private ObjectMapper jackson;
	private EnergyConsumptionService energyConsumptionService;
	private Log logger = LogFactory.getLog(KafkaMessageHandler.class);

	public KafkaMessageHandler(EnergyConsumptionService energyConsumptionService) {
		this.energyConsumptionService = energyConsumptionService;
    }

	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}

	@Bean
	public Consumer<String> sink() {
		return stringInput -> {
			try {
					MessageBoundary message = this.jackson.readValue(stringInput, MessageBoundary.class);
					if (message.getMessageType().equals("deviceNotification") && message.getMessageDetails() != null) {
						String deviceJson = jackson.writeValueAsString(message.getMessageDetails().get("device"));
						DeviceBoundary deviceBoundary = jackson.readValue(deviceJson, DeviceBoundary.class);
						this.energyConsumptionService.handleDeviceEvent(deviceBoundary);
				}
			} catch (Exception e) {
				this.logger.error(e.getMessage());
			}
		};
	}
}
