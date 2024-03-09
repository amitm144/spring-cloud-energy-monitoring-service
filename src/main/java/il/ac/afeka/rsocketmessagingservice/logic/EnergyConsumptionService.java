package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface EnergyConsumptionService {
    Mono<MessageBoundary> getLiveConsumption();
    Mono<Void> handleDeviceEvent(DeviceBoundary deviceBoundary);
    Mono<MessageBoundary> getLiveConsumptionSummary();
    Mono<MessageBoundary> getDailySummary(LocalDateTime day);
    Mono<MessageBoundary> getConsumptionSummaryByMonth(LocalDateTime date);
    Flux<MessageBoundary> getConsumptionWarnings();
    Flux<MessageBoundary> getOverCurrentWarnings();
    void checkForOverCurrent(DeviceBoundary deviceDetails);
    void checkForOverConsumption();
}
