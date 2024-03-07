package il.ac.afeka.rsocketmessagingservice.repositories;

import il.ac.afeka.rsocketmessagingservice.data.DeviceEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DeviceNotificationRepository extends ReactiveMongoRepository<DeviceEntity, String> {
}