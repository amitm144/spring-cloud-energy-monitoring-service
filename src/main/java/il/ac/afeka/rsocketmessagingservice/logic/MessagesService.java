package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessagesService {
    Flux<MessageBoundary> getAll();
    Mono<MessageBoundary> create(MessageBoundary messageBoundary);
    Mono<MessageBoundary> getById(String messageId);
    Flux<MessageBoundary> getByExternalReferences(Flux<ExternalReferenceBoundary> references);
    Mono<Void> deleteAll();
}
