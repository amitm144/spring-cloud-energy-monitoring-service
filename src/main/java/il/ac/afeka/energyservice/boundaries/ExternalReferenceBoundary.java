package il.ac.afeka.energyservice.boundaries;

import il.ac.afeka.energyservice.data.ExternalReferenceEntity;

public class ExternalReferenceBoundary {
	private String service;
	private String externalServiceId;

	public ExternalReferenceBoundary() {}

	public ExternalReferenceBoundary(ExternalReferenceEntity entity) {
		this.setService(entity.getService());
		this.setExternalServiceId(entity.getExternalServiceId());
	}

	public ExternalReferenceEntity toEntity() {
		ExternalReferenceEntity rv = new ExternalReferenceEntity();
		rv.setService(this.getService());
		rv.setExternalServiceId(this.getExternalServiceId());
		return rv;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getExternalServiceId() {
		return externalServiceId;
	}

	public void setExternalServiceId(String externalServiceId) {
		this.externalServiceId = externalServiceId;
	}

	@Override
	public String toString() {
		return "ExternalReferenceBoundary{" +
				"service='" + service + '\'' +
				", externalServiceId='" + externalServiceId + '\'' +
				'}';
	}
}
