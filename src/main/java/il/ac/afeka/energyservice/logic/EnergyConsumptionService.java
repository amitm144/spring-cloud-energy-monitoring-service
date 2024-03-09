package il.ac.afeka.energyservice.logic;

import il.ac.afeka.energyservice.boundaries.DeviceBoundary;
import il.ac.afeka.energyservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface EnergyConsumptionService {
    Mono<Void> handleDeviceEvent(DeviceBoundary deviceBoundary);
    Mono<MessageBoundary> getLiveConsumptionSummary();
    Mono<MessageBoundary> getDailyConsumptionSummary(LocalDateTime day);
    Mono<MessageBoundary> getMonthlyConsumptionSummary(LocalDateTime date);
    Flux<MessageBoundary> getConsumptionWarnings();
    Flux<MessageBoundary> getOverCurrentWarnings();
    void checkForOverCurrent(DeviceBoundary deviceDetails);
    void checkForOverConsumption();
}
