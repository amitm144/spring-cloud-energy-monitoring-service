package il.ac.afeka.rsocketmessagingservice.repositories;

import il.ac.afeka.rsocketmessagingservice.data.ExternalReferenceEntity;
import il.ac.afeka.rsocketmessagingservice.data.MessageEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface MessageRepository extends ReactiveMongoRepository<MessageEntity, String> {
	Flux<MessageEntity> findAllByExternalReferencesIn(List<ExternalReferenceEntity> reference);
}
