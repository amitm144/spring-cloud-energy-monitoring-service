package il.ac.afeka.rsocketmessagingservice.controllers;

import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.IdBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.logic.MessagesService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class MessagesRSocketController {
    private MessagesService messagesService;
    private Log logger = LogFactory.getLog(MessagesRSocketController.class);

    @Autowired
    public void setMessagesService(MessagesService messagesService) { this.messagesService = messagesService; }

    @MessageMapping("${app.rsocket.get-all:get-all-messages}")
    public Flux<MessageBoundary> getAllMessages() {
        this.logger.debug("invoking: get-all-messages");
        return messagesService.getAll();
    }

    @MessageMapping("${app.rsocket.publish:publish-message}")
    public Mono<MessageBoundary> createMessage(@Payload MessageBoundary message) {
        this.logger.debug("invoking: publish-message");
        return messagesService.create(message);
    }

    @MessageMapping("${app.rsocket.get-by-ids:get-messages-by-ids}")
    public Flux<MessageBoundary> getMessagesByIDs(Flux<IdBoundary> ids) {
        this.logger.debug("invoking: get-messages-by-ids");
        return ids.flatMap(id->messagesService.getById(id.getMessageId()));
    }


    @MessageMapping("${app.rsocket.get-by-ext-ref:get-messages-by-external-references}")
    public Flux<MessageBoundary> getMessagesByReferences(Flux<ExternalReferenceBoundary> references) {
        this.logger.debug("invoking: get-messages-by-ext-ref");
        return messagesService.getByExternalReferences(references);
    }

    @MessageMapping("${app.rsocket.delete-all:delete-all-messages}")
    public Mono<Void> deleteAllMessages() {
        this.logger.debug("invoking: delete-all-messages");
        return messagesService.deleteAll();
    }
}
