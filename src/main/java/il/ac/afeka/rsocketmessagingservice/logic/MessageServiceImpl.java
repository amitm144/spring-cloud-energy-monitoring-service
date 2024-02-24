package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Date;

@Service
public class MessageServiceImpl implements MessagesService {
    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public Flux<MessageBoundary> getAll() {
        return this.messageRepository
                .findAll()
                .map(MessageBoundary::new);
    }

    @Override
    public Mono<MessageBoundary> create(MessageBoundary messageBoundary) {
        messageBoundary.setMessageId(null);
        messageBoundary.setPublishedTimestamp(new Date());

        return Mono.just(messageBoundary.toEntity())
                .flatMap(this.messageRepository::save)
                .map(MessageBoundary::new);
    }

    @Override
    public Mono<MessageBoundary> getById(String messageId) {
        return this.messageRepository
                .findById(messageId)
                .map(MessageBoundary::new);
    }

    @Override
    public Flux<MessageBoundary> getByExternalReferences(Flux<ExternalReferenceBoundary> externalReferences) {
        return externalReferences
                .map(ExternalReferenceBoundary::toEntity)
                .collectList()
                .flatMapMany(this.messageRepository::findAllByExternalReferencesIn)
                .map(MessageBoundary::new);
    }

    @Override
    public Mono<Void> deleteAll() {
        return this.messageRepository
                .deleteAll();
    }
}
