package il.ac.afeka.energyservice.controllers;

import il.ac.afeka.energyservice.boundaries.MessageBoundary;
import il.ac.afeka.energyservice.logic.EnergyConsumptionService;
import il.ac.afeka.energyservice.utils.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Controller
public class EnergyMonitoringRSocketController {
    private EnergyConsumptionService energyService;
    private final Log logger = LogFactory.getLog(EnergyMonitoringRSocketController.class);

    @Autowired
    public void setEnergyService(EnergyConsumptionService energyService) { this.energyService = energyService; }

    @MessageMapping("${app.rsocket.event.consumption.live}")
    public Mono<MessageBoundary> publishLiveConsumption() {
        this.logger.debug("live consumption request received");
        return energyService.getLiveConsumptionSummary();
    }

    @MessageMapping("${app.rsocket.event.consumption.summary}")
    public Mono<MessageBoundary> publishConsumptionSummery(LocalDateTime date) {
        if (DateUtils.isValidDate(date.toLocalDate().toString(), "yyyy-MM")) {
            this.logger.debug("publishing monthly consumption summary");
            date = LocalDateTime.of(date.getYear(), date.getMonth(), 1, 0, 0);
            return energyService.getMonthlyConsumptionSummary(date);
        }
        this.logger.debug("publishing daily consumption summary");
        return energyService.getDailyConsumptionSummary(date);
    }

    @MessageMapping("${app.rsocket.event.consumption.warning}")
    public Flux<MessageBoundary> getConsumptionWarnings() {
        this.logger.debug("publishing over-consumption warning");
        return energyService.getConsumptionWarnings();
    }

    @MessageMapping("${app.rsocket.event.overcurrent.warning}")
    public Flux<MessageBoundary> getOverCurrentWarnings() {
        this.logger.debug("publishing over-current warning");
        return energyService.getOverCurrentWarnings();
    }
}