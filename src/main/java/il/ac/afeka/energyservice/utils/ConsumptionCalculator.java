package il.ac.afeka.energyservice.utils;

import il.ac.afeka.energyservice.data.DeviceEntity;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConsumptionCalculator {
    @Value("${house.power.price}")
    private static float PRICE_PER_WATT;

    public static Mono<Float> calculateTotalConsumption(Flux<DeviceEntity> devices) {
        return devices
                .filter(device -> device.getStatus().getIsOn())
                .map(device -> device.getStatus().getCurrentPowerInWatts() * device.getTotalActiveTime())
                .reduce(0.0f, Float::sum);
    }

    public static float calculateEstimatedPrice(float totalConsumption) {
        return totalConsumption * PRICE_PER_WATT;
    }
}
