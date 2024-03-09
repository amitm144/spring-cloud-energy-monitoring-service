package il.ac.afeka.energyservice.logic;

import il.ac.afeka.energyservice.boundaries.DeviceBoundary;
import il.ac.afeka.energyservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EnergyConsumptionService {
    Mono<Void> handleDeviceEvent(DeviceBoundary deviceBoundary);
    Mono<MessageBoundary> getLiveConsumptionSummary();
    Mono<MessageBoundary> getDailyConsumptionSummary(LocalDate date);
    Mono<MessageBoundary> getMonthlyConsumptionSummary(LocalDate date);
    Flux<MessageBoundary> getConsumptionWarnings();
    Flux<MessageBoundary> getOverCurrentWarnings();
    void checkForOverCurrent(DeviceBoundary deviceDetails);
    void checkForOverConsumption();
}
