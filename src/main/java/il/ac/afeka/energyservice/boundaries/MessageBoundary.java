package il.ac.afeka.energyservice.boundaries;

import il.ac.afeka.energyservice.data.MessageEntity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MessageBoundary {
    private String messageId;
    private String summary;
    private String messageType;
    private LocalDateTime publishedTimestamp;
    private Set<ExternalReferenceBoundary> externalReferences;
    private Map<String, Object> messageDetails;

    public MessageBoundary() {}

    public MessageBoundary(MessageEntity entity) {
        this.setMessageId(entity.getMessageId());
        this.setMessageType(entity.getMessageType());
        this.setSummary(entity.getSummary());
        this.setMessageType(entity.getMessageType());
        this.setPublishedTimestamp(entity.getPublishedTimestamp());
        this.setExternalReferences(entity.getExternalReferences()
                                    .stream()
                                    .map(ExternalReferenceBoundary::new)
                                    .collect(Collectors.toSet()));
        this.setMessageDetails(entity.getMessageDetails());
    }

    // Convert this boundary to a MessageEntity object
    public MessageEntity toEntity() {
        MessageEntity rv = new MessageEntity();
        rv.setMessageId(this.getMessageId());
        rv.setMessageType(this.getMessageType());
        rv.setSummary(this.getSummary());
        rv.setPublishedTimestamp(this.getPublishedTimestamp());
        rv.setExternalReferences(this.getExternalReferences()
                                     .stream()
                                     .map(ExternalReferenceBoundary::toEntity)
                                     .collect(Collectors.toSet()));
        rv.setMessageDetails(this.getMessageDetails());
        return rv;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getPublishedTimestamp() {
        return publishedTimestamp;
    }

    public void setPublishedTimestamp(LocalDateTime publishedTimestamp) {
        this.publishedTimestamp = publishedTimestamp;
    }

    public Set<ExternalReferenceBoundary> getExternalReferences() {
        return externalReferences;
    }

    public void setExternalReferences(Set<ExternalReferenceBoundary> externalReferences) {
        this.externalReferences = externalReferences;
    }

    public Map<String, Object> getMessageDetails() {
        return messageDetails;
    }

    public void setMessageDetails(Map<String, Object> messageDetails) {
        this.messageDetails = messageDetails;
    }

    @Override
    public String toString() {
        return "MessageBoundary{" +
                "messageId='" + messageId + '\'' +
                ", summary='" + summary + '\'' +
                ", messageType='" + messageType + '\'' +
                ", publishedTimestamp=" + publishedTimestamp +
                ", externalReferences=" + externalReferences +
                ", messageDetails=" + messageDetails +
                '}';
    }
}