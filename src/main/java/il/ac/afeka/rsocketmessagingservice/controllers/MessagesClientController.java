package il.ac.afeka.rsocketmessagingservice.controllers;

import il.ac.afeka.rsocketmessagingservice.boundaries.IdBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "/messages")
public class MessagesClientController {
    private RSocketRequester requester;
    private RSocketRequester.Builder builder;
    private String socketHost;
    private int socketPort;

    @Value("${app.rsocket.publish:publish-message}")
    private String PUBLISH_MESSAGE_ROUTE;
    @Value("${app.rsocket.get-all:get-all-messages}")
    private String GET_ALL_MESSAGES_ROUTE;
    @Value("${app.rsocket.get-by-ids:get-messages-by-ids}")
    private String GET_MESSAGES_BY_ID_ROUTE;
    @Value("${app.rsocket.get-by-ext-ref:get-messages-by-external-references}")
    private String GET_MESSAGES_BY_EXT_REF_ROUTE;
    @Value("${app.rsocket.delete-all:delete-all-messages}")
    private String DELETE_ALL_MESSAGES_ROUTE;


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

    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<MessageBoundary> publishMessage(@RequestBody MessageBoundary message) {
        return this.requester
                .route(PUBLISH_MESSAGE_ROUTE)
                .data(message)
                .retrieveMono(MessageBoundary.class);
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MessageBoundary> getAll() {
        return this.requester
                .route(GET_ALL_MESSAGES_ROUTE)
                .retrieveFlux(MessageBoundary.class);
    }

    @GetMapping(
            path= {"/byIds/{ids}"},
            produces = {MediaType.TEXT_EVENT_STREAM_VALUE}
    )
    public Flux<MessageBoundary> getMessagesByIDs(@PathVariable String ids) {
        Flux<IdBoundary> idFlux = Flux
                .fromArray(ids.split(","))
                .map(id -> new IdBoundary(id));

        return this.requester
                .route(GET_MESSAGES_BY_ID_ROUTE)
                .data(idFlux)
                .retrieveFlux(MessageBoundary.class);
    }

    // TODO
    public Flux<MessageBoundary> getMessagesByExternalReferences() {

        return this.requester
                .route(GET_MESSAGES_BY_EXT_REF_ROUTE)
//                .data()
                .retrieveFlux(MessageBoundary.class);
    }

    @DeleteMapping
    public Mono<Void> deleteAll() {
        return this.requester
                .route(DELETE_ALL_MESSAGES_ROUTE)
                .send();
    }
}
