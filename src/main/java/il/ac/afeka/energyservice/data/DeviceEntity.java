package il.ac.afeka.energyservice.data;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Map;

public class DeviceEntity {
	@Id
	private String id;
	private String type;
	private String subType;
	private LocalDateTime registrationTimestamp;
	private LocalDateTime lastUpdateTimestamp;
	private String location;
	private int manufacturerPowerInWatts;
	private StatusEntity status;
	private Map<String, Object> additionalAttributes;
	private float totalActiveTime;


	public DeviceEntity() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public LocalDateTime getRegistrationTimestamp() {
		return registrationTimestamp;
	}

	public void setRegistrationTimestamp(LocalDateTime registrationTimestamp) {
		this.registrationTimestamp = registrationTimestamp;
	}

	public LocalDateTime getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(LocalDateTime lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getManufacturerPowerInWatts() {
		return manufacturerPowerInWatts;
	}

	public void setManufacturerPowerInWatts(int manufacturerPowerInWatts) {
		this.manufacturerPowerInWatts = manufacturerPowerInWatts;
	}

	public StatusEntity getStatus() {
		return status;
	}

	public void setStatus(StatusEntity status) {
		this.status = status;
	}

	public Map<String, Object> getAdditionalAttributes() {
		return additionalAttributes;
	}

	public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
		this.additionalAttributes = additionalAttributes;
	}

	public float getTotalActiveTime() {
		return totalActiveTime;
	}

	public void setTotalActiveTime(float totalActiveTime) {
		this.totalActiveTime = totalActiveTime;
	}

	@Override
	public String toString() {
		return "DeviceEntity{" +
				"id='" + id + '\'' +
				", type='" + type + '\'' +
				", subType='" + subType + '\'' +
				", registrationTimestamp=" + registrationTimestamp +
				", lastUpdateTimestamp=" + lastUpdateTimestamp +
				", location='" + location + '\'' +
				", manufacturerPowerInWatts=" + manufacturerPowerInWatts +
				", status=" + status +
				", additionalAttributes=" + additionalAttributes +
				", totalActiveTime=" + totalActiveTime +
				'}';
	}
}
