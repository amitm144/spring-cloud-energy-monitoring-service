package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeviceNotificationService  {
	Mono<MessageBoundary> send(MessageBoundary messageBoundary);
	Mono<DeviceBoundary> storeDeviceNotificationMessage(DeviceBoundary deviceBoundary);
	Flux<DeviceBoundary> getAllDevicesNotificationMessages();
	Mono<Void> cleanup();
}
