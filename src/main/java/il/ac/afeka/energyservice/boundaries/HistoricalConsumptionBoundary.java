package il.ac.afeka.energyservice.boundaries;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HistoricalConsumptionBoundary {

	private LocalDate date;
	private float totalConsumption;

	public HistoricalConsumptionBoundary() {
	}

	public HistoricalConsumptionBoundary(LocalDate date, float totalConsumption) {
		this.date = date;
		this.totalConsumption = totalConsumption;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
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
