package il.ac.afeka.energyservice.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Document(collection = "messages")
public class MessageEntity {
    @Id
    private String messageId;
    private String messageType;
    private String summary;
    private LocalDateTime publishedTimestamp;
    private Set<ExternalReferenceEntity> externalReferences;
    private Map<String, Object> messageDetails;

    public MessageEntity()  {}

    public MessageEntity(String messageId, String messageType, String summary, LocalDateTime publishedTimestamp,
                         Set<ExternalReferenceEntity> externalReferences, Map<String, Object> messageDetails) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.summary = summary;
        this.publishedTimestamp = publishedTimestamp;
        this.externalReferences = externalReferences;
        this.messageDetails = messageDetails;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getSummary() {
        return summary;
    }

    public LocalDateTime getPublishedTimestamp() {
        return publishedTimestamp;
    }

    public Set<ExternalReferenceEntity> getExternalReferences() {
        return externalReferences;
    }

    public Map<String, Object> getMessageDetails() {
        return messageDetails;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setPublishedTimestamp(LocalDateTime publishedTimestamp) {
        this.publishedTimestamp = publishedTimestamp;
    }

    public void setExternalReferences(Set<ExternalReferenceEntity> externalReferences) {
        this.externalReferences = externalReferences;
    }

    public void setMessageDetails(Map<String, Object> messageDetails) {
        this.messageDetails = messageDetails;
    }

    @Override
    public String toString() {
        return "MessageEntity{" +
                "messageId='" + messageId + '\'' +
                ", messageType='" + messageType + '\'' +
                ", summary='" + summary + '\'' +
                ", publishedTimestamp=" + publishedTimestamp +
                ", externalReferences=" + externalReferences +
                ", messageDetails=" + messageDetails +
                '}';
    }
}