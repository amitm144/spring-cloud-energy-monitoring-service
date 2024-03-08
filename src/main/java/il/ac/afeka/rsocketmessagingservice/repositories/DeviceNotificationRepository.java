package il.ac.afeka.rsocketmessagingservice.repositories;

import il.ac.afeka.rsocketmessagingservice.data.DeviceEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.Date;

public interface DeviceNotificationRepository extends ReactiveMongoRepository<DeviceEntity, String> {
    Flux<DeviceEntity> findAllByLastUpdateTimestampAfterAndLastUpdateTimestampBefore(Date before, Date After);
}