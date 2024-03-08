package il.ac.afeka.rsocketmessagingservice.controllers;

import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping(path = "/energy")
public class EnergyMonitoringClientController {
    private RSocketRequester requester;
    private RSocketRequester.Builder builder;
    private String socketHost;
    private int socketPort;

    @Value("${app.rsocket.event.overcurrent.warning}")
    private String OVERCURRENT_WARNING_ROUTE;
    @Value("${app.rsocket.event.consumption.warning}")
    private String CONSUMPTION_WARNING_ROUTE;
    @Value("${app.rsocket.event.consumption.live}")
    private String LIVE_CONSUMPTION_ROUTE;
    @Value("${app.rsocket.event.consumption.summary}")
    private String CONSUMPTION_SUMMARY_ROUTE;

    @Autowired
    public void setBuilder(RSocketRequester.Builder builder) {
        this.builder = builder;
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
    public Flux<MessageBoundary> getConsumptionSummary(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return this.requester
                .route(CONSUMPTION_SUMMARY_ROUTE)
                .data(date)
                .retrieveFlux(MessageBoundary.class);
    }

    @GetMapping("/summary/monthly")
    public Flux<MessageBoundary> getConsumptionSummary(@RequestParam int month) {
        if (month < 1 || month > 12)
            return Flux.error(new RuntimeException("Invalid month")); // Consider custom exception

        return this.requester
                .route(CONSUMPTION_SUMMARY_ROUTE)
                .data(month)
                .retrieveFlux(MessageBoundary.class);
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
}