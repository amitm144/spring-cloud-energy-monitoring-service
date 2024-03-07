package il.ac.afeka.rsocketmessagingservice.logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.repositories.DeviceNotificationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
@Service
public class DeviceNotificationServiceImp implements DeviceNotificationService {

	private final DeviceNotificationRepository deviceNotificationRepository;
//  todo need the dependency of StreamBridge
//	private StreamBridge kafka;
	private ObjectMapper jackson;
	private String targetTopic;

	public DeviceNotificationServiceImp(DeviceNotificationRepository deviceNotificationRepository) {
		this.deviceNotificationRepository = deviceNotificationRepository;
//		this.kafka = kafka;
	}

	@Value("${target.topic.name:topic1}")
	public void setTargetTopic(String targetTopic) {
		this.targetTopic = targetTopic;
	}

	@PostConstruct
	public void init() {
		this.jackson = new ObjectMapper();
	}

	@Override
	public Mono<MessageBoundary> send(MessageBoundary message) {
		try {
			String messageToKafka = this.jackson.writeValueAsString(message);
			return Mono.just(messageToKafka)
//					.map(msg-> kafka.send(targetTopic, msg))
					.then(Mono.just(message));
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Mono<DeviceBoundary> storeDeviceNotificationMessage(DeviceBoundary deviceBoundary) {
		if (deviceBoundary.getId() == null) {
			deviceBoundary.setId(UUID.randomUUID().toString());
		}

		return Mono.just(deviceBoundary)
				.map(DeviceBoundary::toEntity)
				.flatMap(this.deviceNotificationRepository::save)
				.map(DeviceBoundary::new)
				.log();
	}

	@Override
	public Flux<DeviceBoundary> getAllDevicesNotificationMessages() {
		return this.deviceNotificationRepository
				.findAll()
				.map(DeviceBoundary::new)
				.log();
	}

	@Override
	public Mono<Void> cleanup() {
		return this.deviceNotificationRepository
				.deleteAll()
				.log();
	}
}


