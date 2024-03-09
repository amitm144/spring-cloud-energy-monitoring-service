package il.ac.afeka.energyservice.boundaries;

import il.ac.afeka.energyservice.data.StatusEntity;

import java.util.Arrays;

public class StatusBoundary {

	private boolean isOn;
	private int brightness;
	private int[] colorRGB;
	private float currentPowerInWatts;

	public StatusBoundary() {
	}

	public StatusBoundary(boolean isOn, int brightness, int[] colorRGB, float currentPowerInWatts) {
		this.isOn = isOn;
		this.brightness = brightness;
		this.colorRGB = colorRGB;
		this.currentPowerInWatts = currentPowerInWatts;
	}

	public StatusBoundary(StatusEntity statusEntity) {
		this.isOn = statusEntity.isOn();
		this.brightness = statusEntity.getBrightness();
		this.colorRGB = statusEntity.getColorRGB();
		this.currentPowerInWatts = statusEntity.getCurrentPowerInWatts();
	}

	public StatusEntity toEntity() {
		StatusEntity rv = new StatusEntity();
		rv.setIsOn(isOn());
		rv.setBrightness(getBrightness());
		rv.setColorRGB(getColorRGB());
		rv.setCurrentPowerInWatts(getCurrentPowerInWatts());
		return rv;
	}

	public boolean isOn() {
		return isOn;
	}

	public void setIsOn(boolean on) {
		isOn = on;
	}

	public int getBrightness() {
		return brightness;
	}

	public void setBrightness(int brightness) {
		this.brightness = brightness;
	}

	public int[] getColorRGB() {
		return colorRGB;
	}

	public void setColorRGB(int[] colorRGB) {
		this.colorRGB = colorRGB;
	}

	public float getCurrentPowerInWatts() {
		return currentPowerInWatts;
	}

	public void setCurrentPowerInWatts(float currentPowerInWatts) {
		this.currentPowerInWatts = currentPowerInWatts;
	}

	@Override
	public String toString() {
		return "StatusBoundary{" +
				"isOn=" + isOn +
				", brightness=" + brightness +
				", colorRGB=" + Arrays.toString(colorRGB) +
				", currentPowerInWatts=" + currentPowerInWatts +
				'}';
	}
}
