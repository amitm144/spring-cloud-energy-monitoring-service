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
    private Log logger = LogFactory.getLog(MessagesClientController.class);

    @Autowired
    public void setMessagesService(MessagesService messagesService) { this.messagesService = messagesService; }

    @MessageMapping("${app.rsocket.get-all:get-all-messages}")
    public Flux<MessageBoundary> getAllMessages() {
        return null;
    }

    @MessageMapping("${app.rsocket.publish:publish-message}")
    public Mono<MessageBoundary> createMessage(@Payload MessageBoundary message) {
        return null;
    }

    @MessageMapping("${app.rsocket.get-by-ids:get-messages-by-ids}")
    public Flux<MessageBoundary> getMessagesByIDs(Flux<IdBoundary> ids) {
        return null;
    }

    @MessageMapping("${app.rsocket.get-by-ext-ref:get-messages-by-external-references}")
    public Flux<MessageBoundary> getMessagesByReferences(Flux<ExternalReferenceBoundary> references) {
        return null;
    }

    @MessageMapping("${app.rsocket.delete-all:delete-all-messages}")
    public Mono<Void> deleteAllMessages() {
        return null;
    }
}
