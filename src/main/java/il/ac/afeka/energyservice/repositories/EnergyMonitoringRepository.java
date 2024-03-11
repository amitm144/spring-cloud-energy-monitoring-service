package il.ac.afeka.energyservice.repositories;

import il.ac.afeka.energyservice.data.MessageEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface EnergyMonitoringRepository extends ReactiveMongoRepository<MessageEntity, String> {
    Flux<MessageEntity> findAllByMessageType(String messageType);
}
