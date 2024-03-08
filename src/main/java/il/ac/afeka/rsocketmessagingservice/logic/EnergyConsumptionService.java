package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

public interface EnergyConsumptionService {
    Mono<MessageBoundary> getLiveConsumption();
    Mono<Void> handleDeviceEvent(DeviceBoundary deviceBoundary);
    Flux<MessageBoundary> getLiveConsumptionSummery();
    Flux<MessageBoundary> getConsumptionSummaryByDay(Date day);
    Flux<MessageBoundary> getConsumptionSummaryByMonth(Date date);
    Flux<MessageBoundary> generateConsumptionWarning(float currentConsumption);
    Flux<MessageBoundary> generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption);
}
