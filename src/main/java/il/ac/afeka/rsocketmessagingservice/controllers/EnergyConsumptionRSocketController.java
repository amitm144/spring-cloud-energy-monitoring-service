package il.ac.afeka.rsocketmessagingservice.controllers;

import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.logic.EnergyConsumptionsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class EnergyConsumptionRSocketController {
    private EnergyConsumptionsService messagesService;
    private Log logger = LogFactory.getLog(EnergyConsumptionRSocketController.class);

    @Autowired
    public void setMessagesService(EnergyConsumptionsService messagesService) { this.messagesService = messagesService; }

    @MessageMapping("${new-house-event}")
    public Mono<MessageBoundary> createNewHouse(@Payload MessageBoundary message) {
        this.logger.debug("invoking: newHouseEvent");
        return messagesService.createNewHouse(message);
    }
    @MessageMapping("${device-event}")
    public Mono<Void> HandleDeviceEvent(@Payload MessageBoundary message) {
        this.logger.debug("invoking: newHouseEvent");
        return messagesService.HandleDeviceEvent(message);
    }
    @MessageMapping("${live-consumption}")
    public Flux<MessageBoundary> GetCurrentConsumptionSummery(Flux<MessageBoundary> messages) {
        this.logger.debug("invoking: live--consumption");
        return messagesService.GetCurrentConsumptionSummery(messages);
    }
    @MessageMapping("${consumption-summary-event}")
    public Flux<MessageBoundary> GetConsumptionSummery(Flux<MessageBoundary> messages) {
        this.logger.debug("invoking: consumption-summary-event");
        return messagesService.GetConsumptionSummery(messages);
    }
}
