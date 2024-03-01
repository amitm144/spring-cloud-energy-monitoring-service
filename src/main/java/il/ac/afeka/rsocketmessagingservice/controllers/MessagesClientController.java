package il.ac.afeka.rsocketmessagingservice.controllers;

import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@RestController
@RequestMapping(path = "/messages")
public class MessagesClientController {
    private RSocketRequester requester;
    private RSocketRequester.Builder builder;
    private String socketHost;
    private int socketPort;

    @Value("${overcurrent-warning-event}")
    private String OVERCURRENT_WARNING_EVENT;
    @Value("${consumption-warning-event}")
    private String CONSUMPTION_WARNING_EVENT;
    @Value("${new-house-event}")
    private String newHouseEventRoute;
    @Value("${device-event}")
    private String deviceEventRoute;
    @Value("${live-consumption}")
    private String liveConsumptionRoute;
    @Value("${consumption-summary-event}")
    private String consumptionSummaryEventRoute;

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

    @GetMapping(path ="/over-current-warning")
    public Flux<MessageBoundary> getOverCurrentWarningEvent() {
        // demo - Warning
        MessageBoundary warrning =generateOvercurrentWarning("0","Light bulb",5f);
        return this.requester
                .route(OVERCURRENT_WARNING_EVENT)
                .data(warrning)
                .retrieveFlux(MessageBoundary.class);
    }

    @GetMapping(path ="/consumption-warning")
    public Flux<MessageBoundary> getConsumptionWarningEvent() {
        MessageBoundary warrning =generateConsumptionWarning(20000f);
        // demo - Warning
        return this.requester
                .route(CONSUMPTION_WARNING_EVENT)
                .data(warrning)
                .retrieveFlux(MessageBoundary.class);
    }

    @PostMapping(path ="/new-house")
    public Mono<MessageBoundary> createNewHouse(@RequestBody MessageBoundary message) {
        return this.requester.route(newHouseEventRoute)
                .data(message)
                .retrieveMono(MessageBoundary.class);
    }

    @PostMapping(path ="/device-event")
    public Mono<Void> handleDeviceEvent(@RequestBody MessageBoundary message) {
        return this.requester.route(deviceEventRoute)
                .data(message)
                .send();
    }

    @GetMapping(path ="/current-consumption")
    public Flux<MessageBoundary> getCurrentConsumptionSummary(@RequestBody List<MessageBoundary> messages) {
        return this.requester.route(liveConsumptionRoute)
                .data(Flux.fromIterable(messages))
                .retrieveFlux(MessageBoundary.class);
    }

    @GetMapping(path ="/consumption-summary")
    public Flux<MessageBoundary> getConsumptionSummary(@RequestBody List<MessageBoundary> messages) {
        return this.requester.route(consumptionSummaryEventRoute)
                .data(Flux.fromIterable(messages))
                .retrieveFlux(MessageBoundary.class);
    }
    private MessageBoundary generateOvercurrentWarning(String deviceId, String deviceType, float currentConsumption) {
        MessageBoundary overcurrentWarning = new MessageBoundary();
        overcurrentWarning.setMessageId(UUID.randomUUID().toString());
        overcurrentWarning.setPublishedTimestamp(new Date());
        overcurrentWarning.setMessageType("overcurrentWarning");
        overcurrentWarning.setSummary("device " + deviceId + " is over consuming");

        // Set external references
        ExternalReferenceBoundary externalReference = new ExternalReferenceBoundary();
        externalReference.setExternalServiceId("1");
        externalReference.setService("PowerManagementService");
        Set<ExternalReferenceBoundary> refs = new HashSet<>();
        refs.add(externalReference);
        overcurrentWarning.setExternalReferences(refs);

        // Set message details
        HashMap<String, Object> details = new HashMap<>();
        details.put("houseId", "houseXYZ");
        details.put("deviceId", deviceId);
        details.put("deviceType", deviceType);
        details.put("currentConsumption", currentConsumption);
        overcurrentWarning.setMessageDetails(details);

        return overcurrentWarning;
    }
    private MessageBoundary generateConsumptionWarning(float currentConsumption) {
        MessageBoundary consumptionWarning = new MessageBoundary();
        consumptionWarning.setMessageId(UUID.randomUUID().toString());
        consumptionWarning.setPublishedTimestamp(new Date());
        consumptionWarning.setMessageType("consumptionWarning");
        consumptionWarning.setSummary("You have reached your average daily consumption");

        // Set external references
        ExternalReferenceBoundary externalReference = new ExternalReferenceBoundary();
        externalReference.setExternalServiceId("2");
        externalReference.setService("EnergyUsageService");
        Set<ExternalReferenceBoundary> refs = new HashSet<>();
        refs.add(externalReference);
        consumptionWarning.setExternalReferences(refs);

        // Set message details
        HashMap<String, Object> details = new HashMap<>();
        details.put("houseId", "houseXYZ");
        details.put("currentConsumption", currentConsumption);
        consumptionWarning.setMessageDetails(details);

        return consumptionWarning;
    }

}