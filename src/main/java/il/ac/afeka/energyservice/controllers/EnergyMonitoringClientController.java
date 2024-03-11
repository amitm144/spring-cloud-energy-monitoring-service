package il.ac.afeka.energyservice.controllers;

import il.ac.afeka.energyservice.boundaries.DeviceBoundary;
import il.ac.afeka.energyservice.boundaries.MessageBoundary;
import il.ac.afeka.energyservice.services.messaging.MessageQueueHandler;
import il.ac.afeka.energyservice.utils.DateUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(path = "/energy")
public class EnergyMonitoringClientController {
    private RSocketRequester requester;
    private RSocketRequester.Builder builder;
    private MessageQueueHandler kafka;
    private String socketHost;
    private int socketPort;

    @Value("${app.rsocket.event.overcurrent.warning}")
    private String OVERCURRENT_WARNING_ROUTE;
    @Value("${app.rsocket.event.consumption.warning}")
    private String CONSUMPTION_WARNING_ROUTE;
    @Value("${app.rsocket.event.consumption.live}")
    private String LIVE_CONSUMPTION_ROUTE;
    @Value("${app.rsocket.event.consumption.summary.monthly}")
    private String MONTHLY_CONSUMPTION_SUMMARY_ROUTE;
    @Value("${app.rsocket.event.consumption.summary.daily}")
    private String DAILY_CONSUMPTION_SUMMARY_ROUTE;
    //for debugging purposes
    @Value("${app.rsocket.event.device.save}")
    private String SAVE_DEVICE_ROUTE;
    @Value("${app.rsocket.event.device.getAll}")
    private String GET_ALL_DEVICES_ROUTE;
    @Value("${app.rsocket.event.device.deleteAll}")
    private String DELETE_ALL_DEVICES_ROUTE;


    @Autowired
    public void setBuilder(RSocketRequester.Builder builder, MessageQueueHandler kafka) {
        this.builder = builder;
        this.kafka=kafka;
    }

    @Value("${app.rsocket.host:127.0.0.1}")
    public void setSocketHost(String socketHost) {
        this.socketHost = socketHost;
    }

    @Value("${spring.rsocket.server.port:7001}")
    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }

    @PostConstruct
    public void init() {
        this.requester = this.builder.tcp(socketHost, socketPort);
    }

    @GetMapping("/summary")
    public Mono<MessageBoundary> getConsumptionSummary() {
        return this.requester
                .route(LIVE_CONSUMPTION_ROUTE)
                .retrieveMono(MessageBoundary.class);
    }

    @GetMapping("/summary/daily")
    public Mono<MessageBoundary> getDailyConsumptionSummary(@RequestParam String date) {
        if (!DateUtils.isValidDate(date, "yyyy-MM-dd"))
            return Mono.error(new RuntimeException("Invalid date provided"));

        return this.requester
                .route(DAILY_CONSUMPTION_SUMMARY_ROUTE)
                .data(date)
                .retrieveMono(MessageBoundary.class);
    }

    @GetMapping("/summary/monthly")
    public Mono<MessageBoundary> getMonthlyConsumptionSummary(@RequestParam String date) {
        try {
            LocalDate parsedDate = DateUtils.parseDate(date, "yyyy-MM-dd").withDayOfMonth(1);
            if (parsedDate.isAfter(LocalDate.now())) {
                return Mono.error(new RuntimeException("Invalid date provided"));
            }

            return this.requester
                    .route(MONTHLY_CONSUMPTION_SUMMARY_ROUTE)
                    .data(date)
                    .retrieveMono(MessageBoundary.class);
        } catch (DateTimeParseException e) {
            return Mono.error(new RuntimeException("Invalid date provided"));
        }
    }

    @GetMapping(path ="/warning/overcurrent")
    public Flux<MessageBoundary> getAllOverCurrentWarningEvents() {
        return this.requester
                .route(OVERCURRENT_WARNING_ROUTE)
                .retrieveFlux(MessageBoundary.class);
    }

    @GetMapping(path ="/warning/consumption")
    public Flux<MessageBoundary> getAllConsumptionWarningEvent() {
        return this.requester
                .route(CONSUMPTION_WARNING_ROUTE)
                .retrieveFlux(MessageBoundary.class);
    }

    @PostMapping(path ="/kafka/message")
    public Mono<Void> getTestKafka(@RequestBody MessageBoundary message) {
        return this.kafka.publish(message);
    }

    @PostMapping("/device")
    public Mono<DeviceBoundary> saveDevice(@RequestBody DeviceBoundary device) {
        return this.requester
                .route(SAVE_DEVICE_ROUTE)
                .data(device)
                .retrieveMono(DeviceBoundary.class);
    }

    @GetMapping("/devices")
    public Flux<DeviceBoundary> getAllDevices() {
        return this.requester
                .route(GET_ALL_DEVICES_ROUTE)
                .retrieveFlux(DeviceBoundary.class);
    }
    @DeleteMapping("/devices")
    public Mono<Void> deleteAllDevices() {
        return this.requester
                .route(DELETE_ALL_DEVICES_ROUTE)
                .retrieveMono(Void.class);
    }
}