package il.ac.afeka.energyservice.services.messaging;

import reactor.core.publisher.Mono;

import java.util.function.Consumer;

public interface MessageQueueHandler {
    Consumer<String> sink();
    Mono<Void> publish(Object data);
}
