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
        return energyService.getLiveConsumption();
    }

    @MessageMapping("${app.rsocket.event.consumption.summary}")
    public Flux<MessageBoundary> publishConsumptionSummery() {
        this.logger.debug("publishing consumption summary");
        return energyService.getConsumptionSummaryByDay(new Date());
    }

    @MessageMapping("${app.rsocket.event.consumption.warning}")
    public Flux<MessageBoundary> getConsumptionWarnings() {
        this.logger.debug("publishing over-consumption warning");
        return energyService.getConsumptionWarnings() ;
    }

    @MessageMapping("${app.rsocket.event.overcurrent.warning}")
    public Flux<MessageBoundary> getOverCurrentWarnings() {
        this.logger.debug("publishing over-current warning");
        return energyService.getOverCurrentWarnings();
    }
}