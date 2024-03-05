package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

public interface EnergyConsumptionsService {
    Mono<MessageBoundary> getLiveConsumption();
    Mono<Void> handleDeviceEvent(MessageBoundary message);
    Flux<MessageBoundary> getLiveConsumptionSummery();
    Flux<MessageBoundary> getConsumptionSummaryByDay(Date day);
    Flux<MessageBoundary> getConsumptionSummaryByMonth(Date date);
    Flux<MessageBoundary> generateConsumptionWarning(float currentConsumption);
    Flux<MessageBoundary> generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption);

    //kafka
    Mono<MessageBoundary> send(MessageBoundary message);
    Mono<MessageBoundary> store(MessageBoundary message);
    Flux<MessageBoundary> getAll();
    Mono<Void> cleanup();
}
