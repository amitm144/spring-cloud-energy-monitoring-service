package il.ac.afeka.rsocketmessagingservice.kafkaMessagesListener;

import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;

public interface KafkaMessageImp {
    public void messageSink();
    public void sendMessageToKafka(MessageBoundary message);
}
