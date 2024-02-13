package il.ac.afeka.rsocketmessagingservice.repositories;

import il.ac.afeka.rsocketmessagingservice.data.MessageEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface MessageRepository extends ReactiveMongoRepository<MessageEntity, String> {
}
