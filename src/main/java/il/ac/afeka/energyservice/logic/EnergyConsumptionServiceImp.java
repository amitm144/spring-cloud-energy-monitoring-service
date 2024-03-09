package il.ac.afeka.energyservice.logic;

import il.ac.afeka.energyservice.boundaries.DeviceBoundary;
import il.ac.afeka.energyservice.boundaries.MessageBoundary;
import il.ac.afeka.energyservice.data.DeviceEntity;
import il.ac.afeka.energyservice.data.ExternalReferenceEntity;
import il.ac.afeka.energyservice.data.MessageEntity;
import il.ac.afeka.energyservice.services.messaging.MessageQueueHandler;
import il.ac.afeka.energyservice.repositories.DeviceDataRepository;
import il.ac.afeka.energyservice.repositories.EnergyMonitoringRepository;
import il.ac.afeka.energyservice.utils.ConsumptionCalculator;
import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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

import static il.ac.afeka.energyservice.utils.DateUtils.isLastDayOfMonth;

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

    public EnergyConsumptionServiceImp(@Lazy MessageQueueHandler messageHandler, DeviceDataRepository deviceDataRepository,
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
                    // Generate daily summary and issue to message queue
                    generateDailySummary(LocalDate.now())
                            .subscribe(this.messageHandler::publish);

                    // Generate monthly summary if needed and issue to message queue
                    if (isLastDayOfMonth()) {
                        generateMonthlySummary(LocalDate.now())
                                .subscribe(this.messageHandler::publish);
                    }
                }
            } catch (Exception e) {
                // If the thread is interrupted, log the exception
                Thread.currentThread().interrupt();
                this.logger.error("Thread "
                        + Thread.currentThread().threadId()
                        + " was interrupted, failed to complete operation: "
                        + e.getMessage()
                );
            }
            // Code to be executed after waking up
            this.logger.debug(LocalDateTime.now() + " : thread id " + Thread.currentThread().threadId() + " woke up");
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
    public Mono<MessageBoundary> getLiveConsumptionSummary() {
        return Mono.just(generateLiveSummary());
    }

    @Override
    public Mono<MessageBoundary> getDailyConsumptionSummary(LocalDate date) {
        return generateDailySummary(date);
    }

    @Override
    public Mono<MessageBoundary> getMonthlyConsumptionSummary(LocalDate date) {
        return this.generateMonthlySummary(date);
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
            this.messageHandler.publish(overCurrentMessage);
        }
    }

    @Override
    public void checkForOverConsumption() {
        float consumptionInWatts = (float) this.generateLiveSummary().getMessageDetails().getOrDefault("consumption", 0f);
        if (consumptionInWatts >= OVERCONSUMPTION_LIMIT) {
            MessageBoundary overConsumptionMessage = generateConsumptionWarning(consumptionInWatts);
            this.messageHandler.publish(overConsumptionMessage);
        }
    }

    private MessageBoundary generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption) {
        MessageEntity overCurrentWarning = new MessageEntity();
        overCurrentWarning.setMessageId(UUID.randomUUID().toString());
        overCurrentWarning.setPublishedTimestamp(LocalDateTime.now());
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
        consumptionWarning.setPublishedTimestamp(LocalDateTime.now());
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

    private Mono<MessageBoundary> generateDailySummary(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
        Flux<DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(startOfDay, endOfDay);

        Flux<DeviceEntity> onDevices = devices.filter(device -> device.getStatus().isOn())
            .map(device-> {
                float remainingTimeOnInHours = Duration.between(device.getLastUpdateTimestamp(), LocalDateTime.now()).toHours();
                device.setTotalActiveTime(device.getTotalActiveTime() + remainingTimeOnInHours);
                return device;
            });

        Mono<Float> totalConsumptionMono = calculateConsumptionForDay(date);
        Mono<Float> expectedBillMono = totalConsumptionMono.map(ConsumptionCalculator::calculateEstimatedPrice);

        Mono<MessageBoundary> dailySummary = totalConsumptionMono
                .zipWith(expectedBillMono)
                .flatMap(tuple -> {
                    Float totalConsumption = tuple.getT1();
                    Float expectedBill = tuple.getT2();

                    MessageEntity summary = new MessageEntity();
                    summary.setMessageType("dailyConsumptionSummary");
                    summary.setPublishedTimestamp(LocalDateTime.now());
                    summary.setSummary("Your total power consumption for today is " + totalConsumption + "W");

                    // Set external references
                    ExternalReferenceEntity externalReference = createDefaultExternalReferenceEntity();
                    Set<ExternalReferenceEntity> refs = new HashSet<>();
                    refs.add(externalReference);
                    summary.setExternalReferences(refs);

                    Map<String, Object> messageDetails = new HashMap<>();
                    messageDetails.put("totalConsumption:", totalConsumption);
                    messageDetails.put("expectedBill:", expectedBill);
                    summary.setMessageDetails(messageDetails);

                    return this.energyMonitoringRepository.save(summary);
                })
                .map(MessageBoundary::new);

        onDevices.map(device -> {
                device.setTotalActiveTime(0);
                device.setLastUpdateTimestamp(LocalDateTime.now());
                return device;
            })
            .map(deviceDataRepository::save);

        return dailySummary;
    }

    private Mono<MessageBoundary> generateMonthlySummary(LocalDate date) {
        // Calculate total consumption for the specified month
        Mono<Float> totalConsumptionMono = calculateConsumptionForMonth(date);
        Mono<Float> expectedBillMono = totalConsumptionMono.map(ConsumptionCalculator::calculateEstimatedPrice);

        return totalConsumptionMono.zipWith(expectedBillMono)
                .flatMap(tuple -> {
                    Float totalConsumption = tuple.getT1();
                    Float expectedBill = tuple.getT2();

                    MessageEntity summary = new MessageEntity();
                    summary.setMessageType("monthlyConsumptionSummary");
                    summary.setPublishedTimestamp(LocalDateTime.now());
                    summary.setSummary("Your total power consumption for " + date.getMonth() + " is " + totalConsumption + "W");

                    // Set external references
                    ExternalReferenceEntity externalReference = createDefaultExternalReferenceEntity();
                    Set<ExternalReferenceEntity> refs = new HashSet<>();
                    refs.add(externalReference);
                    summary.setExternalReferences(refs);

                    Map<String, Object> messageDetails = new HashMap<>();
                    messageDetails.put("totalConsumption", totalConsumption);
                    messageDetails.put("expectedBill", expectedBill);
                    summary.setMessageDetails(messageDetails);

                    return energyMonitoringRepository.save(summary);
                })
                .map(MessageBoundary::new);
    }

    private MessageBoundary generateLiveSummary() {
        //TODO the function need to get also the list of location that we also write in the summary the consumption according the location.
        MessageEntity summary = new MessageEntity();
        Mono<Float> totalConsumption = calculateTotalLiveConsumption();

        summary.setPublishedTimestamp(LocalDateTime.now());
        summary.setMessageType("dailyConsumptionSummary");
        summary.setSummary("Your is currently consuming " + totalConsumption + "W");

        // Set external references
        ExternalReferenceEntity externalReference = createDefaultExternalReferenceEntity();
        Set<ExternalReferenceEntity> refs = new HashSet<>();
        refs.add(externalReference);
        summary.setExternalReferences(refs);

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("current consumption:", totalConsumption);
        summary.setMessageDetails(messageDetails);

        this.energyMonitoringRepository.save(summary);
        return new MessageBoundary(summary);
    }

    private Mono<MessageBoundary> createDailySummary(LocalDate date) {
        // Calculate total consumption for the specified day
        Mono<Float> totalConsumptionMono = calculateConsumptionForDay(date);
        Mono<Float> expectedBillMono = totalConsumptionMono.map(ConsumptionCalculator::calculateEstimatedPrice);

        return totalConsumptionMono.zipWith(expectedBillMono)
                .flatMap(tuple -> {
                    Float totalConsumption = tuple.getT1();
                    Float expectedBill = tuple.getT2();

                    MessageEntity summary = new MessageEntity();
                    summary.setMessageType("dailyConsumptionSummary");
                    summary.setPublishedTimestamp(LocalDateTime.now());
                    summary.setSummary("Your total power consumption for today is " + totalConsumption + "W");

                    // Set external references
                    ExternalReferenceEntity externalReference = createDefaultExternalReferenceEntity();
                    Set<ExternalReferenceEntity> refs = new HashSet<>();
                    refs.add(externalReference);
                    summary.setExternalReferences(refs);

                    Map<String, Object> messageDetails = new HashMap<>();
                    messageDetails.put("totalConsumption:", totalConsumption);
                    messageDetails.put("expectedBill:", expectedBill);
                    summary.setMessageDetails(messageDetails);

                    return this.energyMonitoringRepository.save(summary);
                })
                .map(MessageBoundary::new);
    }

    public Mono<Float> calculateTotalLiveConsumption () {
        return deviceDataRepository.findAll()
                .filter(device -> device.getStatus().isOn())
                .map(d -> d.getStatus().getCurrentPowerInWatts())
                .reduce(0.0f,Float::sum);
    }

    public Mono<Float> calculateTotalLiveConsumptionByLocation(String location) {
        return deviceDataRepository.findAll()
                .filter(device -> device.getStatus().isOn() && device.getLocation().equals(location))
                .map(d -> d.getStatus().getCurrentPowerInWatts())
                .reduce(0.0f,Float::sum);
    }

    private long calculateSleepTimeUntilMidnightInMilliseconds() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().atTime(LocalTime.MIDNIGHT).plusDays(1);
        return Duration.between(now, nextMidnight).toMillis();
    }

    private Mono<Float> calculateConsumptionForDay(LocalDate date) {
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(23, 59, 59);

        Flux<DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(startDate,endDate);
        return ConsumptionCalculator.calculateTotalConsumption(devices);
    }

    private Mono<Float> calculateConsumptionForMonth(LocalDate date) {
        LocalDateTime firstDayOfMonth = date.withDayOfMonth(1).atStartOfDay();
        LocalDateTime lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        Flux<DeviceEntity> devices = deviceDataRepository.findAllByLastUpdateTimestampBetween(firstDayOfMonth,lastDayOfMonth);
        return ConsumptionCalculator.calculateTotalConsumption(devices);
    }

    private ExternalReferenceEntity createDefaultExternalReferenceEntity() {
        ExternalReferenceEntity externalReference = new ExternalReferenceEntity();
        externalReference.setExternalServiceId(EXT_SERVICE_ID);
        externalReference.setService(SERVICE_NAME);
        return externalReference;
    }
}
