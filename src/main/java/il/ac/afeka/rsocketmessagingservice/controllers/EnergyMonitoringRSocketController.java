package il.ac.afeka.rsocketmessagingservice.controllers;

import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.logic.EnergyConsumptionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;

@Controller
public class EnergyMonitoringRSocketController {
    private EnergyConsumptionService energyService;
    private final Log logger = LogFactory.getLog(EnergyMonitoringRSocketController.class);

    @Autowired
    public void setEnergyService(EnergyConsumptionService energyService) { this.energyService = energyService; }

    @MessageMapping("${app.rsocket.event.consumption.live}")
    public Mono<MessageBoundary> publishLiveConsumption() {
        this.logger.debug("live consumption request received");
        return energyService.getLiveConsumptionSummery();
    }

    @MessageMapping("${app.rsocket.event.consumption.summary}")
    public Mono<MessageBoundary> publishConsumptionSummeryByDay() {
        // TODO: change to daily and monthly kafka event
        this.logger.debug("publishing consumption summary");
        return energyService.getDailySummary(LocalDateTime.now());
    }

    @MessageMapping("${app.rsocket.event.consumption.summary}")
    public Mono<MessageBoundary> publishConsumptionSummeryByMonth() {
        // TODO: change to daily and monthly kafka event
        this.logger.debug("publishing consumption summary");
        return energyService.getConsumptionSummaryByMonth(LocalDateTime.now());
    }

    @MessageMapping("${app.rsocket.event.consumption.warning}")
    public Flux<MessageBoundary> publishConsumptionWarning() {
        // TODO: change to kafka event
        this.logger.debug("publishing over-consumption warning");
        return energyService.generateConsumptionWarning(20.7f);
    }

    @MessageMapping("${app.rsocket.event.overcurrent.warning}")
    public Flux<MessageBoundary> publishOverCurrentWarning() {
        // TODO: change to kafka event
        this.logger.debug("publishing over-current warning");
        return energyService.generateOverCurrentWarning("abc-123", "LED Lamp", 7.2f);
    }
}