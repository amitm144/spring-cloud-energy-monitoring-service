package il.ac.afeka.energyservice.boundaries;

import il.ac.afeka.energyservice.data.DeviceEntity;

import java.time.LocalDateTime;
import java.util.Map;

public class DeviceBoundary {
	private String id;
	private String type;
	private String subType;
	private LocalDateTime registrationTimestamp;
	private LocalDateTime lastUpdateTimestamp;
	private String location;
	private int manufacturerPowerInWatts;
	private StatusBoundary status;
	private Map<String, Object> additionalAttributes;

	public DeviceBoundary() {}

	public DeviceBoundary(DeviceEntity deviceEntity) {
		this.id = deviceEntity.getId();
		this.type = deviceEntity.getType();
		this.subType = deviceEntity.getSubType();
		this.registrationTimestamp = deviceEntity.getRegistrationTimestamp();
		this.lastUpdateTimestamp = deviceEntity.getLastUpdateTimestamp();
		this.location = deviceEntity.getLocation();
		this.manufacturerPowerInWatts = deviceEntity.getManufacturerPowerInWatts();
		this.status = new StatusBoundary(deviceEntity.getStatus());
		this.additionalAttributes = deviceEntity.getAdditionalAttributes();
	}

	public DeviceEntity toEntity() {
		DeviceEntity rv = new DeviceEntity();
		rv.setId(this.getId());
		rv.setType(this.getType());
		rv.setSubType(this.getSubType());
		rv.setRegistrationTimestamp(this.getRegistrationTimestamp());
		rv.setLastUpdateTimestamp(this.getLastUpdateTimestamp());
		rv.setLocation(this.getLocation());
		rv.setManufacturerPowerInWatts(this.getManufacturerPowerInWatts());
		rv.setStatus(this.getStatus().toEntity());
		rv.setAdditionalAttributes(this.getAdditionalAttributes());
		return rv;
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

	public StatusBoundary getStatus() {
		return status;
	}

	public void setStatus(StatusBoundary status) {
		this.status = status;
	}

	public Map<String, Object> getAdditionalAttributes() {
		return additionalAttributes;
	}

	public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
		this.additionalAttributes = additionalAttributes;
	}

	@Override
	public String toString() {
		return "DeviceBoundary{" +
				"id='" + id + '\'' +
				", type='" + type + '\'' +
				", subType='" + subType + '\'' +
				", registrationTimestamp=" + registrationTimestamp +
				", lastUpdateTimestamp=" + lastUpdateTimestamp +
				", location='" + location + '\'' +
				", manufacturerPowerInWatts=" + manufacturerPowerInWatts +
				", status=" + status +
				", additionalAttributes=" + additionalAttributes +
				'}';
	}
}


