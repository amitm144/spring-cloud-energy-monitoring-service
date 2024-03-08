package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.data.DeviceEntity;
import il.ac.afeka.rsocketmessagingservice.repositories.DeviceNotificationRepository;
import il.ac.afeka.rsocketmessagingservice.repositories.EnergyMonitoringRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class EnergyConsumptionServiceImp implements EnergyConsumptionService {
    private final EnergyMonitoringRepository energyMonitoringRepository;
    private final DeviceNotificationRepository deviceNotificationRepository;

    public EnergyConsumptionServiceImp(EnergyMonitoringRepository energyMonitoringRepository,
                                       DeviceNotificationRepository deviceNotificationRepository) {
        this.energyMonitoringRepository = energyMonitoringRepository;
        this.deviceNotificationRepository = deviceNotificationRepository;
    }
    @PostConstruct
    void startDailyThread()
    {
        Thread vt = Thread.startVirtualThread(() -> {
            // This block runs in a virtual thread
            System.err.println(LocalDateTime.now() + " : thread id " + Thread.currentThread().getId() + " start and sleep");
            // Calculate time until midnight
            //TimeUnit
            try {
                // Sleep until midnight
                while(true)
                {
                    long sleepTime = calculateSleepTimeUntilMidnight();
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    UpdateDevices();
                }
            } catch (InterruptedException e) {
                // If the thread is interrupted, handle the exception
                Thread.currentThread().interrupt();
                System.err.println("Thread was interrupted, failed to complete operation");
            }

            // Code to be executed after waking up, e.g., database read/update

            System.err.println(LocalDateTime.now() + " : thread id " + Thread.currentThread().getId() + " woke up, completing tasks.");
        });

        // Optionally, you can store the thread reference or add additional logic
    }

    private void UpdateDevices() {
        Flux<DeviceEntity> devices =
        deviceNotificationRepository
                .findAll();
        devices
            .filter(device -> device.getStatus().isOn())        //Case Device is ON
            .map(device->{
                Date date = new Date();            //LastUpdate 00:00
                float total = date.getTime() - device.getLastUpdateTimestamp().getTime();
                device.setTotalActiveTime(device.getTotalActiveTime()+total);        //SetTotal
                device.setLastUpdateTimestamp(date);
                return device;
            });

        Mono<Float> dailyTotalTime =
            devices
                .map(d->d.getTotalActiveTime())
                    .reduce(0.0f,Float::sum);     //Update DailyTotal
            devices
                .map(device -> {
                    device.setTotalActiveTime(0);
                    return device;
                })
                .flatMap(deviceNotificationRepository::save); // Ensure reset devices are saved
        DailySummeryBoundary summery =createDailySummeryBoundary(dailyTotalTime);

        //send to Kafka message

    }

    private DailySummeryBoundary createDailySummeryBoundary(Mono<Float> dailyTotalTime) {return null;
    }

    private long calculateSleepTimeUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().atTime(LocalTime.MIDNIGHT).plusDays(1);
        return Duration.between(now, nextMidnight).toMillis();
    }
    @Override
    public Mono<Void> handleDeviceEvent(DeviceBoundary deviceBoundary) {
        return deviceNotificationRepository.findById(deviceBoundary.getId())
                .flatMap(deviceNotification -> {
                    // Device exists, set totalActiveTime
                    if (!deviceNotification.getStatus().isOn()){
                        deviceNotification.setTotalActiveTime(
                                deviceNotification.getTotalActiveTime() +
                                        getNewTime(deviceBoundary.getLastUpdateTimestamp(),
                                                deviceNotification.getLastUpdateTimestamp()));
                    }
                    // Save the updated deviceNotification
                    return deviceNotificationRepository.save(deviceNotification);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Device does not exist, set totalActiveTime to 0
                    DeviceEntity deviceNotification = deviceBoundary.toEntity();
                    deviceNotification.setTotalActiveTime(0);
                    return deviceNotificationRepository.save(deviceNotification);
                }))
                .then(Mono.empty());
    }

    private float getNewTime(Date oldTime, Date newTime) {
        return ((float)newTime.getTime() - (float)oldTime.getTime()) * 360000;
    }

    @Override
    public Mono<MessageBoundary> getLiveConsumption() {
        return null;
    }


    @Override
    public Flux<MessageBoundary> getLiveConsumptionSummery() {
        return null;
    }

    @Override
    public Flux<MessageBoundary> getConsumptionSummaryByDay(Date day) {
        return null;
    }

    @Override
    public Flux<MessageBoundary> getConsumptionSummaryByMonth(Date date) {
        return null;
    }

    private Mono<DeviceEntity> saveToRepository(DeviceBoundary deviceBoundary){
        if (deviceBoundary.getId() == null) {
            deviceBoundary.setId(UUID.randomUUID().toString());
        }
        return this.deviceNotificationRepository.save(deviceBoundary.toEntity());
    }

    public Flux<MessageBoundary> generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption) {
        MessageBoundary overCurrentWarning = new MessageBoundary();
        overCurrentWarning.setMessageId(UUID.randomUUID().toString());
        overCurrentWarning.setPublishedTimestamp(new Date());
        overCurrentWarning.setMessageType("overcurrentWarning");
        overCurrentWarning.setSummary("device " + deviceId + " is over consuming");

        // Set external references
        ExternalReferenceBoundary externalReference = new ExternalReferenceBoundary();
        externalReference.setExternalServiceId("1");
        externalReference.setService("PowerManagementService");
        Set<ExternalReferenceBoundary> refs = new HashSet<>();
        refs.add(externalReference);
        overCurrentWarning.setExternalReferences(refs);

        // Set message details
        HashMap<String, Object> details = new HashMap<>();
        details.put("houseId", "houseXYZ");
        details.put("deviceId", deviceId);
        details.put("deviceType", deviceType);
        details.put("currentConsumption", currentConsumption);
        overCurrentWarning.setMessageDetails(details);

        return Flux.just(overCurrentWarning);
    }
    public Flux<MessageBoundary> generateConsumptionWarning(float currentConsumption) {
        MessageBoundary consumptionWarning = new MessageBoundary();
        consumptionWarning.setMessageId(UUID.randomUUID().toString());
        consumptionWarning.setPublishedTimestamp(new Date());
        consumptionWarning.setMessageType("consumptionWarning");
        consumptionWarning.setSummary("You have reached your average daily consumption");

        // Set external references
        ExternalReferenceBoundary externalReference = new ExternalReferenceBoundary();
        externalReference.setExternalServiceId("2");
        externalReference.setService("EnergyUsageService");
        Set<ExternalReferenceBoundary> refs = new HashSet<>();
        refs.add(externalReference);
        consumptionWarning.setExternalReferences(refs);

        // Set message details
        HashMap<String, Object> details = new HashMap<>();
        details.put("houseId", "houseXYZ");
        details.put("currentConsumption", currentConsumption);
        consumptionWarning.setMessageDetails(details);

        return Flux.just(consumptionWarning);
    }
}
