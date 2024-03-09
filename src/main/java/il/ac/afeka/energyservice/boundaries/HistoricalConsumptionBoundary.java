package il.ac.afeka.energyservice.boundaries;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HistoricalConsumptionBoundary {

	private LocalDateTime date;
	private float totalConsumption;

	public HistoricalConsumptionBoundary() {
	}

	public HistoricalConsumptionBoundary(LocalDateTime date, float totalConsumption) {
		this.date = date;
		this.totalConsumption = totalConsumption;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public float getTotalConsumption() {
		return totalConsumption;
	}

	public void setTotalConsumption(float totalConsumption) {
		this.totalConsumption = totalConsumption;
	}

	@Override
	public String toString() {
		return "historicalConsumptionBoundary{" +
				"date=" + date +
				", totalConsumption=" + totalConsumption +
				'}';
	}
}
