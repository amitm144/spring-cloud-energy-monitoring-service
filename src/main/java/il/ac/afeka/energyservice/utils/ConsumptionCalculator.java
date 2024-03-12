package il.ac.afeka.energyservice.utils;

import il.ac.afeka.energyservice.data.DeviceEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ConsumptionCalculator {
    private static float PRICE_PER_WATT;

    @Autowired
    public ConsumptionCalculator(@Value("${house.power.price}") float pricePerWatt) {
        PRICE_PER_WATT = pricePerWatt;
    }

    public static Mono<Float> calculateTotalConsumption(Flux<DeviceEntity> devices) {
        return devices
                .map(device -> device.getStatus().getCurrentPowerInWatts() * device.getTotalActiveTime())
                .reduce(0.0f, Float::sum);
    }

    public static float calculateEstimatedPrice(float totalConsumption) {
        return totalConsumption * PRICE_PER_WATT;
    }
}
