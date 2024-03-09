package il.ac.afeka.rsocketmessagingservice.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.data.DeviceEntity;
import il.ac.afeka.rsocketmessagingservice.data.ExternalReferenceEntity;
import il.ac.afeka.rsocketmessagingservice.data.MessageEntity;
import il.ac.afeka.rsocketmessagingservice.messageHandler.MessageQueueHandler;
import il.ac.afeka.rsocketmessagingservice.repositories.DeviceDataRepository;
import il.ac.afeka.rsocketmessagingservice.repositories.EnergyMonitoringRepository;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static il.ac.afeka.rsocketmessagingservice.utils.DateUtils.isLastDayOfMonth;

@Service
public class EnergyConsumptionServiceImp implements EnergyConsumptionService {
    private final EnergyMonitoringRepository energyMonitoringRepository;
    private final DeviceDataRepository deviceDataRepository;
    private final MessageQueueHandler messageHandler;
    private final Log logger = LogFactory.getLog(EnergyConsumptionServiceImp.class);

    @Value("${house.consumption.limit:8000}")
    private float OVERCONSUMPTION_LIMIT;
    @Value("${house.current.limit:15}")
    private float OVERCURRENT_LIMIT;
    @Value("${house.voltage:220}")
    private float HOUSEHOLD_VOLTAGE;
    @Value("${spring.application.id}")
    private String EXT_SERVICE_ID;
    @Value("${spring.application.name}")
    private String SERVICE_NAME;

    public EnergyConsumptionServiceImp(MessageQueueHandler messageHandler, DeviceDataRepository deviceDataRepository,
                                       EnergyMonitoringRepository energyMonitoringRepository) {
        this.energyMonitoringRepository = energyMonitoringRepository;
        this.deviceDataRepository = deviceDataRepository;
        this.messageHandler = messageHandler;
    }

    @PostConstruct
    public void startDailyThread() {
        Thread vt = Thread.startVirtualThread(() -> {
            // Calculate time until midnight
            long sleepTime = calculateSleepTimeUntilMidnightInMilliseconds();
            try {
                while(true) {
                    // Sleep until midnight
                    TimeUnit.MILLISECONDS.sleep(sleepTime);
                    // Generate daily summary
                    MessageBoundary summary = generateDailySummary();
                    // issue daily consumption summary
                    this.messageHandler.publish(summary);
                    // Generate monthly summary if needed and issue it
                    if (isLastDayOfMonth()) {
                        summary = generateMonthlySummary();
                        this.messageHandler.publish(summary);
                    }
                }
            } catch (InterruptedException | JsonProcessingException e) {
                // If the thread is interrupted, log the exception
                Thread.currentThread().interrupt();
                this.logger.error("Thread "
                        + Thread.currentThread().getId()
                        + " was interrupted, failed to complete operation: "
                        + e.getMessage()
                );
            }
            // Code to be executed after waking up
            this.logger.debug(LocalDateTime.now() + " : thread id " + Thread.currentThread().getId() + " woke up");
        });
    }

    @Override
    public Mono<Void> handleDeviceEvent(DeviceBoundary deviceEvent) {
        return deviceDataRepository.findById(deviceEvent.getId())
                .flatMap(device -> {
                    // Device exists, calculate remainder of time on and reset totalActiveTime
                    if (!device.getStatus().isOn()) {
                        float totalTimeOn = Duration.between(device.getLastUpdateTimestamp(),
                                deviceEvent.getLastUpdateTimestamp()).toHours();
                        device.setTotalActiveTime(device.getTotalActiveTime() + totalTimeOn);
                    }
                    // Save the updated device data
                    return deviceDataRepository.save(device);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Device does not exist, set totalActiveTime to 0
                    DeviceEntity deviceNotification = deviceEvent.toEntity();
                    deviceNotification.setTotalActiveTime(0.0f);
                    return deviceDataRepository.save(deviceNotification);
                }))
                .then(Mono.empty());
    }

    @Override
    public Mono<MessageBoundary> getLiveConsumption() {
        return null;
    }

    @Override
    public Mono<MessageBoundary> getLiveConsumptionSummary() {
        return Mono.just(createLiveSummary());
    }

    @Override
    public Mono<MessageBoundary> getDailySummary(LocalDateTime date) {
        return Mono.just(createDailySummary(date));
    }

    @Override
    public Mono<MessageBoundary> getConsumptionSummaryByMonth(LocalDateTime date) {
        return Mono.just(createMonthlySummary(date));
    }

    @Override
    public Flux<MessageBoundary> getConsumptionWarnings() {
        return this.energyMonitoringRepository.findAllByMessageType("consumptionWarning")
                .map(MessageBoundary::new);
    }

    @Override
    public Flux<MessageBoundary> getOverCurrentWarnings() {
        return this.energyMonitoringRepository.findAllByMessageType("overcurrentWarning")
                .map(MessageBoundary::new);
    }

    @Override
    public void checkForOverCurrent(DeviceBoundary deviceDetails) {
        if (!deviceDetails.getStatus().isOn())
            return;

        float deviceCurrentConsumption = deviceDetails.getStatus().getCurrentPowerInWatts() / HOUSEHOLD_VOLTAGE;
        if (deviceCurrentConsumption > OVERCURRENT_LIMIT) {
            MessageBoundary overCurrentMessage = generateOverCurrentWarning(deviceDetails.getId(),
                    deviceDetails.getSubType(), deviceCurrentConsumption);
            try {
                this.messageHandler.publish(overCurrentMessage);
            } catch (JsonProcessingException e) {
                this.logger.error(e.getMessage());
            }
        }
    }

    @Override
    public void checkForOverConsumption() {
//        this.getLiveConsumption()
//                .map(MessageBoundary::getMessageDetails)
//                .map(map -> map.getOrDefault("consumption", 0))
//                .
    }

    private MessageBoundary generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption) {
        MessageEntity overCurrentWarning = new MessageEntity();
        overCurrentWarning.setMessageId(UUID.randomUUID().toString());
        overCurrentWarning.setPublishedTimestamp(new Date());
        overCurrentWarning.setMessageType("overcurrentWarning");
        overCurrentWarning.setSummary("device " + deviceId + " is over consuming");

        // Set external references
        ExternalReferenceEntity externalReference = createDefaultExternalReferenceEntity();
        Set<ExternalReferenceEntity> refs = new HashSet<>();
        refs.add(externalReference);
        overCurrentWarning.setExternalReferences(refs);

        // Set message details
        HashMap<String, Object> details = new HashMap<>();
        details.put("deviceId", deviceId);
        details.put("deviceType", deviceType);
        details.put("currentConsumption", currentConsumption);
        overCurrentWarning.setMessageDetails(details);

        this.energyMonitoringRepository.save(overCurrentWarning);
        return new MessageBoundary(overCurrentWarning);
    }

    private MessageBoundary generateConsumptionWarning(float currentConsumption) {
        MessageEntity consumptionWarning = new MessageEntity();
        consumptionWarning.setMessageId(UUID.randomUUID().toString());
        consumptionWarning.setPublishedTimestamp(new Date());
        consumptionWarning.setMessageType("consumptionWarning");
        consumptionWarning.setSummary("You have reached your daily consumption limit");

        // Set external references
        ExternalReferenceEntity externalReference = createDefaultExternalReferenceEntity();
        Set<ExternalReferenceEntity> refs = new HashSet<>();
        refs.add(externalReference);
        consumptionWarning.setExternalReferences(refs);

        // Set message details
        HashMap<String, Object> details = new HashMap<>();
        details.put("currentConsumption", currentConsumption);
        consumptionWarning.setMessageDetails(details);

        this.energyMonitoringRepository.save(consumptionWarning);
        return new MessageBoundary(consumptionWarning);
    }

    private MessageBoundary generateDailySummary() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Flux<DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(startOfDay, endOfDay);

        devices.filter(device -> device.getStatus().isOn())
                .map(device-> {
                    float remainingTimeOnInHours = Duration.between(device.getLastUpdateTimestamp(), LocalDateTime.now()).toHours();
                    device.setTotalActiveTime(device.getTotalActiveTime() + remainingTimeOnInHours);
                    return device;
                });

        LocalDateTime now = LocalDateTime.now();
        MessageBoundary dailySummary = createDailySummary(now);
        devices
                .map(device -> {
                    device.setTotalActiveTime(0);
                    device.setLastUpdateTimestamp(now);
                    return device;
                })
                .map(deviceDataRepository::save);

        return dailySummary;
    }

    private MessageBoundary generateMonthlySummary() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String publishedTimestamp = date.format(formatter);

        // Calculate total consumption for the specified day
        Mono <Float> totalConsumption = calculateConsumptionForMonth(date);
        float bill = totalConsumption.block() * 0.0006145f;

        MessageBoundary summary = new MessageBoundary();
        summary.setMessageType("monthConsumptionSummary");
        summary.setPublishedTimestamp(date);
        summary.setSummary("Your house consumed " + totalConsumption + " W/h on this month is" + date.getMonth().toString());
        summary.setExternalReferences(createExternalReferences());

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("totalConsumption:", totalConsumption);
        messageDetails.put("expectedBill:", bill);
        summary.setMessageDetails(messageDetails);

        return summary;
    }

    private MessageBoundary createLiveSummary() {
        //TODO the function need to get also the list of location that we also write in the summary the consumption according the location.

//                "externalReferences":[
//        {
//            "service":"string",
//                "externalServiceId":"string"
//        }
//  ],
//        "messageDetails":{
//            "houseId": "strin  ng",
//                    "consumption": float,
//            "consumptionByLocation": [
//            {
//                "Location": "string",
//                "consumption": float,
//            }


        MessageBoundary summary = new MessageBoundary();
        Mono <Float> totalConsumption = calculateTotalLiveConsumption();

        summary.setPublishedTimestamp(LocalDateTime.now());
        summary.setMessageType("dailyConsumptionSummary");
        summary.setSummary("Your house consumed " + totalConsumption + " W/h on " + LocalDateTime.now());
        summary.setExternalReferences(createExternalReferences());

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("current consumption:", totalConsumption);
        summary.setMessageDetails(messageDetails);

        return summary;
    }

    public Mono<Float> calculateTotalLiveConsumption () {
        return deviceDataRepository.findAll()
                .filter(device -> device.getStatus().isOn()) // Filter devices with status = true
                .map(d -> d.getStatus().getCurrentPowerInWatts()) // Map each device to its consumption
                .reduce(0.0f,Float::sum); // Sum up all the consumptions
    }

    public Mono<Float> calculateTotalLiveConsumptionByLocation(String location) {
        return deviceDataRepository.findAll()
                .filter(device -> device.getStatus().isOn()) // Filter devices with status = true
                .filter(device -> device.getLocation().equals(location))
                .map(d -> d.getStatus().getCurrentPowerInWatts()) // Map each device to its consumption
                .reduce(0.0f,Float::sum); // Sum up all the consumptions
    }

    private long calculateSleepTimeUntilMidnightInMilliseconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().atTime(LocalTime.MIDNIGHT).plusDays(1);
        return Duration.between(now, nextMidnight).toMillis();
    }

    private Mono<Float> calculateConsumptionForDay(LocalDateTime date) {
        LocalDateTime startDate = date.toLocalDate().atStartOfDay()
                .with(LocalTime.of(0, 0, 0))
                .plusMinutes(1);

        LocalDateTime endDate = date.toLocalDate().atTime(23, 59, 59);

        Flux <DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(startDate,endDate); //need to check

        return  devices.map(d -> d.getStatus().getCurrentPowerInWatts()* d.getTotalActiveTime()) // Map each device to its consumption
                .reduce(0.0f, Float::sum);
    }

    private Mono<Float> calculateConsumptionForMonth(LocalDateTime date) {
        LocalDateTime firstDayOfMonth = date.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        Flux <DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(firstDayOfMonth,lastDayOfMonth); //need to check

        return  devices.map(d -> d.getStatus().getCurrentPowerInWatts()* d.getTotalActiveTime()) // Map each device to its consumption
                .reduce(0.0f, Float::sum);
    }

    private ExternalReferenceEntity createDefaultExternalReferenceEntity() {
        ExternalReferenceEntity externalReference = new ExternalReferenceEntity();
        externalReference.setExternalServiceId(EXT_SERVICE_ID);
        externalReference.setService(SERVICE_NAME);
        return externalReference;
    }
}