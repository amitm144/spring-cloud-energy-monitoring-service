package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Date;

public interface EnergyConsumptionsService {
    Mono<Void> handleDeviceEvent(MessageBoundary message);
    Mono<MessageBoundary> getLiveConsumptionSummery();
    Mono<MessageBoundary> getDailySummary(LocalDate day);
    Mono<MessageBoundary> getConsumptionSummaryByMonth(Date date);
    Flux<MessageBoundary> generateConsumptionWarning(float currentConsumption);
    Flux<MessageBoundary> generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption);
}
