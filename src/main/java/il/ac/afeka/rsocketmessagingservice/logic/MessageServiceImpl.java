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
        return null;
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
    public Flux<MessageBoundary> getById(String messageId) {
        return null;
    }

    @Override
    public Flux<MessageBoundary> getByExternalReference(ExternalReferenceBoundary reference) {
        return null;
    }

    @Override
    public Mono<Void> deleteAll() {
        return null;
    }
}
