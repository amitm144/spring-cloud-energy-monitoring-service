package il.ac.afeka.rsocketmessagingservice.repositories;

import il.ac.afeka.rsocketmessagingservice.data.DeviceEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

public interface DeviceDataRepository extends ReactiveMongoRepository<DeviceEntity, String> {
    Flux<DeviceEntity> findAllByLastUpdateTimestampBetween(LocalDateTime start, LocalDateTime end);


}