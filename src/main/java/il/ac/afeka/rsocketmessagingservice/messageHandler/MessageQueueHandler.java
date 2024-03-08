package il.ac.afeka.rsocketmessagingservice.messageHandler;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Consumer;

public interface MessageQueueHandler {
    Consumer<String> sink();
    void publish(Object data) throws JsonProcessingException;
}
