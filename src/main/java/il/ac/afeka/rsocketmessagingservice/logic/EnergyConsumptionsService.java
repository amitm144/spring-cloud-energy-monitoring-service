package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnergyConsumptionsService {

    Mono<MessageBoundary> createNewHouse(MessageBoundary message);

    Mono<Void> HandleDeviceEvent(MessageBoundary message);

    Flux<MessageBoundary> GetCurrentConsumptionSummery(Flux<MessageBoundary> references);

    Flux<MessageBoundary> GetConsumptionSummery(Flux<MessageBoundary> references);
}
