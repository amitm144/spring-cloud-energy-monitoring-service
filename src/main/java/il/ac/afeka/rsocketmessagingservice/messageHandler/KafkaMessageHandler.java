package il.ac.afeka.rsocketmessagingservice.messageHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.logic.EnergyConsumptionService;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class KafkaMessageHandler implements MessageQueueHandler {
	private EnergyConsumptionService energyConsumptionService;
	private final StreamBridge kafkaProducer;

	private ObjectMapper jackson;
	private String targetTopic;
	private Log logger = LogFactory.getLog(KafkaMessageHandler.class);

	public KafkaMessageHandler(EnergyConsumptionService energyConsumptionService, StreamBridge kafkaProducer) {
		this.energyConsumptionService = energyConsumptionService;
        this.kafkaProducer = kafkaProducer;
    }

	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}

	@Value("${target.topic.name:topic1}")
	public void setTargetTopic(String targetTopic) {
		this.targetTopic = targetTopic;
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
						this.energyConsumptionService.checkForOverCurrent(deviceBoundary);
						this.energyConsumptionService.checkForOverConsumption();
				}
			} catch (Exception e) {
				this.logger.error(e.getMessage());
			}
		};
	}

	@Bean
	public void publish(Object data) throws JsonProcessingException {
		this.jackson.writeValueAsString(data);
		this.kafkaProducer.send(this.targetTopic, data);
	}
}
