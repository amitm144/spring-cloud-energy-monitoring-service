package il.ac.afeka.rsocketmessagingservice.data;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Document(collation = "messages")
public class MessageEntity {
    @Id
    private String messageId;
    private String messageType;
    private String summary;
    private Date publishedTimestamp;
    private Set<ExternalReference> externalReferences;
    private Map<String, Object> messageDetails;
    public MessageEntity()  {}
    public MessageEntity(String messageId, String messageType, String summary, Date publishedTimestamp,
                         Set<ExternalReference> externalReferences, Map<String, Object> messageDetails) {
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

    public Date getPublishedTimestamp() {
        return publishedTimestamp;
    }

    public Set<ExternalReference> getExternalReferences() {
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

    public void setPublishedTimestamp(Date publishedTimestamp) {
        this.publishedTimestamp = publishedTimestamp;
    }

    public void setExternalReferences(Set<ExternalReference> externalReferences) {
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
