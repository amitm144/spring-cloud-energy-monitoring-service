package il.ac.afeka.rsocketmessagingservice.kafkaMessagesListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.logic.EnergyConsumptionService;
import il.ac.afeka.rsocketmessagingservice.logic.EnergyConsumptionServiceImp;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Configuration
public class KafkaMessageHandler {
	private ObjectMapper jackson;
	private EnergyConsumptionService energyConsumptionService;
	private Log logger = LogFactory.getLog(KafkaMessageHandler.class);
	private final StreamBridge kafka;

	@Value("${target.topic.name:topic1}")
	private String targetTopic;
	@Value("${target.topic.name:anyTopic}")
	public void setTargetTopic(String targetTopic) {
		this.targetTopic = targetTopic;
	}

	public KafkaMessageHandler(EnergyConsumptionService energyConsumptionService) {
		this.energyConsumptionService = energyConsumptionService;
	}

	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}

	@Bean
	public Consumer<String> messageSink() {
		return stringInput -> {
			try {
				MessageBoundary message = this.jackson.readValue(stringInput, MessageBoundary.class);
				if (message.getMessageType().equals("deviceNotification") &
						message.getMessageDetails() != null) {

					String deviceJson = jackson.writeValueAsString(message.getMessageDetails().get("device"));
					DeviceBoundary deviceBoundary = jackson.readValue(deviceJson, DeviceBoundary.class);
					this.energyConsumptionService.handleDeviceEvent(deviceBoundary);
				}
			} catch (Exception e) {
				e.printStackTrace();
				this.logger.error(e);
			}
		};
	}
	// Send message to Kafka
	public Mono<Void> sendMessageToKafka(MessageBoundary message) {
		try {
			String messageToKafka = this.jackson.writeValueAsString(message);
			kafka.send(A, message);
			logger.info("Sent message to Kafka: {}", messageToKafka);
		} catch (Exception e) {
			logger.error("Error sending message: {}", e.getMessage());
		}

		return Mono.empty();
	}

}
