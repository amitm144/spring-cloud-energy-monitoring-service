package il.ac.afeka.energyservice.controllers;

import il.ac.afeka.energyservice.boundaries.DeviceBoundary;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Controller
public class EnergyMonitoringRSocketController {
    private EnergyConsumptionService energyService;
    private final Log logger = LogFactory.getLog(EnergyMonitoringRSocketController.class);

    @Autowired
    public void setEnergyService(EnergyConsumptionService energyService) { this.energyService = energyService; }

    @MessageMapping("${app.rsocket.event.device}")
    public Mono<Void> handleDeviceEvent(DeviceBoundary event) {
        return this.energyService.handleDeviceEvent(event);
    }

    @MessageMapping("${app.rsocket.event.consumption.live}")
    public Mono<MessageBoundary> publishLiveConsumption() {
        this.logger.debug("live consumption request received");
        return energyService.getLiveConsumptionSummary();
    }

    @MessageMapping("${app.rsocket.event.consumption.summary.daily}")
    public Mono<MessageBoundary> getDailyConsumptionSummery(String date) {
        try {
            LocalDate parsedDate = DateUtils.parseDate(date, "yyyy-MM-dd");
            if (parsedDate.isAfter(LocalDate.now())) {
                return Mono.error(new RuntimeException("Invalid date provided"));
            }
            this.logger.debug("publishing daily consumption summary");
            return energyService.getDailyConsumptionSummary(parsedDate);
        } catch (DateTimeParseException e) {
            return Mono.error(new RuntimeException("Invalid date provided"));
        }
    }

    @MessageMapping("${app.rsocket.event.consumption.summary.monthly}")
    public Mono<MessageBoundary> getMonthlyConsumptionSummery(String date) {
        try {
            LocalDate parsedDate = DateUtils.parseDate(date, "yyyy-MM-dd");
            if (parsedDate.isAfter(LocalDate.now())) {
                return Mono.error(new RuntimeException("Invalid date provided"));
            }
            this.logger.debug("publishing monthly consumption summary");
            return energyService.getMonthlyConsumptionSummary(parsedDate);
        } catch (DateTimeParseException e) {
            return Mono.error(new RuntimeException("Invalid date provided"));
        }
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