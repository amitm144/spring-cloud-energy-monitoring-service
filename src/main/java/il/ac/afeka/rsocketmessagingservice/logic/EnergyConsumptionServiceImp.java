package il.ac.afeka.rsocketmessagingservice.logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.afeka.rsocketmessagingservice.boundaries.DeviceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.data.DeviceEntity;
import il.ac.afeka.rsocketmessagingservice.data.MessageEntity;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

import static il.ac.afeka.rsocketmessagingservice.utils.DateUtils.isLastDayOfMonth;

@Service
public class EnergyConsumptionServiceImp implements EnergyConsumptionService {
    private final EnergyMonitoringRepository energyMonitoringRepository;
    private final DeviceDataRepository deviceDataRepository;
    private final StreamBridge kafkaProducer;
    private final ObjectMapper jackson;
    private String targetTopic;

    private Log logger = LogFactory.getLog(EnergyConsumptionServiceImp.class);


    public EnergyConsumptionServiceImp(StreamBridge kafkaProducer, DeviceDataRepository deviceDataRepository,
                                       EnergyMonitoringRepository energyMonitoringRepository) {
        this.energyMonitoringRepository = energyMonitoringRepository;
        this.deviceDataRepository = deviceDataRepository;
        this.kafkaProducer = kafkaProducer;
        this.jackson = new ObjectMapper();
    }

    @Value("${target.topic.name:topic1}")
    public void setTargetTopic(String targetTopic) {
        this.targetTopic = targetTopic;
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
                    MessageBoundary summary = generateDailySummary();
                    String summaryMessage = this.jackson.writeValueAsString(summary);
                    this.kafkaProducer.send(this.targetTopic, summaryMessage);

                    if (isLastDayOfMonth()) {
                        summary = generateMonthlySummary();
                        summaryMessage = this.jackson.writeValueAsString(summary);
                        this.kafkaProducer.send(this.targetTopic, summaryMessage);
                    }
                }
            } catch (InterruptedException | JsonProcessingException e) {
                // If the thread is interrupted, handle the exception
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
                    // Device exists, set totalActiveTime
                    if (!device.getStatus().isOn()) {
                        float totalTimeOn = Duration.between(device.getLastUpdateTimestamp(),
                                deviceEvent.getLastUpdateTimestamp()).toHours();
                        device.setTotalActiveTime(device.getTotalActiveTime() + totalTimeOn);
                    }
                    // Save the updated device
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
    public Mono<MessageBoundary> getLiveConsumptionSummery() {

    }

    @Override
    public Mono<MessageBoundary> getDailySummary(LocalDateTime date) {

    }

    @Override
    public Mono<MessageBoundary> getConsumptionSummaryByMonth(LocalDateTime date) {
        return null;
    }

    public Flux<MessageBoundary> generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption) {
        MessageBoundary overCurrentWarning = new MessageBoundary();
        overCurrentWarning.setMessageId(UUID.randomUUID().toString());
        overCurrentWarning.setPublishedTimestamp(LocalDateTime.now());
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
        consumptionWarning.setPublishedTimestamp(LocalDateTime.now());
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

    private MessageBoundary generateDailySummary() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Flux<DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(startOfDay, endOfDay);

        devices.filter(d -> d.getStatus().isOn())
                .map(device-> {
                    float totalTimeOn = Duration.between(device.getLastUpdateTimestamp(), LocalDateTime.now()).toHours();
                    device.setTotalActiveTime(device.getTotalActiveTime() + totalTimeOn);
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
        return null;
    }

    private MessageBoundary createLiveSummary() {

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
        summary.setExternalReferences();

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("current consumption:", totalConsumption);

        return summary;
    }

    private MessageBoundary createDailySummary(LocalDateTime date) {         // Get current date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String publishedTimestamp = date.format(formatter);

        // Calculate total consumption for the specified day
        Mono <Float> totalConsumption = calculateConsumptionForDay(date);
        float bill = totalConsumption.block() * 0.0006145f;

        MessageEntity summary = new MessageEntity();
        summary.setMessageType("dailyConsumptionSummary");
        summary.setPublishedTimestamp(date);
        summary.setSummary("Your house consumed " + totalConsumption + " W/h on " + date.toString());
        summary.setExternalReferences();

        summary.setMessageDetails(messageDetails);
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("totalConsumption:", totalConsumption);
        messageDetails.put("expectedBill:", bill);

        return summary;
    }

    private long calculateSleepTimeUntilMidnightInMilliseconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().atTime(LocalTime.MIDNIGHT).plusDays(1);
        return Duration.between(now, nextMidnight).toMillis();
    }

    public Mono<Float> calculateTotalLiveConsumption () {
        return deviceDataRepository.findAll()
                .filter(device -> device.getStatus().isOn()) // Filter devices with status = true
                .map(d -> d.getStatus().getCurrentPowerInWatts()) // Map each device to its consumption
                .reduce(0.0f,Float::sum); // Sum up all the consumptions
    }

    public Mono<Float> calculateTotalLiveConsumptionByRoom (String location) {
        return deviceDataRepository.findAll()
                .filter(device -> device.getStatus().isOn()) // Filter devices with status = true
                .filter(device -> device.getLocation().equals(location))
                .map(d -> d.getStatus().getCurrentPowerInWatts()) // Map each device to its consumption
                .reduce(0.0f,Float::sum); // Sum up all the consumptions
    }

    public Mono<Float> calculateConsumptionForDay(LocalDateTime date) {
        LocalDateTime startDate = date.toLocalDate().atStartOfDay()
                .with(LocalTime.of(0, 0, 0))
                .plusMinutes(1);

        LocalDateTime endDate = date.toLocalDate().atTime(23, 59, 59);

        Flux <DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(startDate,endDate); //need to check

       return  devices.map(d -> d.getStatus().getCurrentPowerInWatts()* d.getTotalActiveTime()) // Map each device to its consumption
                .reduce(0.0f, Float::sum);
    }
}
