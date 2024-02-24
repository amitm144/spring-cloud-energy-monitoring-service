package il.ac.afeka.rsocketmessagingservice.boundaries;

public class IdBoundary {
	private String messageId;

	public IdBoundary() {
	}

	public IdBoundary(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	@Override
	public String toString() {
		return "IdBoundary{" +
				"messageId='" + messageId + '\'' +
				'}';
	}
}
