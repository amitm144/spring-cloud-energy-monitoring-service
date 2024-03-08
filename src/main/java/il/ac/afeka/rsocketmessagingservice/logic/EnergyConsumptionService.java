package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.data.DeviceEntity;
import il.ac.afeka.rsocketmessagingservice.data.MessageEntity;
import il.ac.afeka.rsocketmessagingservice.repositories.EnergyMonitoringRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EnergyConsumptionService implements EnergyConsumptionsService {
    private final EnergyMonitoringRepository energyMonitoringRepository;
    private final DeviceNotificationService deviceNotificationService;


    public EnergyConsumptionService(EnergyMonitoringRepository energyMonitoringRepository, DeviceNotificationService deviceNotificationService) {
        this.energyMonitoringRepository = energyMonitoringRepository;
        this.deviceNotificationService = deviceNotificationService;
    }

    @Override
    public Mono<Void> handleDeviceEvent(MessageBoundary message) {
        return null; // Fire and Forget returns nothing
    }

    @Override
    public Mono<MessageBoundary> getLiveConsumptionSummery() {
        Map<String, Object> summary = new HashMap<>();
        Mono <Float> consumption = calculateTotalLiveConsumption();

        summary.put("messageId:", new MessageBoundary().getMessageId());
        summary.put("publishedTimestamp:", new Date().toString());
        summary.put("messageType:", "liveConsumptionSummary");
        summary.put("summary:", "Your house is currently consuming" + consumption +"W/h.");
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("current consumption:", consumption);
        summary.put("messageDetails:", messageDetails);
        return Mono.just(new MessageBoundary(summary));

    }

    @Override
    public Mono<MessageEntity> getDailySummary(LocalDateTime date) {
        // Get current date
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String publishedTimestamp = now.format(formatter);

        // Calculate total consumption for the specified day
        Mono <Float> totalConsumption = calculateConsumptionForDay(date);

        // Construct the summary message
        Map<String, Object> summary = new HashMap<>();
        summary.put("messageId:", new MessageBoundary().getMessageId());
        summary.put("publishedTimestamp:", publishedTimestamp);
        summary.put("messageType:", "dailyConsumptionSummary");
        summary.put("summary:", "Your house consumed " + totalConsumption + " W/h on " + date.toString());
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("consumption:", totalConsumption);
        summary.put("messageDetails:", messageDetails);

        return Mono.just(new MessageBoundary(summary)).log();
    }

    @Override
    public Mono<MessageBoundary> getConsumptionSummaryByMonth(LocalDateTime date) {
        // Get current date
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String publishedTimestamp = now.format(formatter);

        // Calculate total consumption for the specified month
        float totalConsumption = calculateMonthlyConsumptionForDate(date);

        // Construct the summary message
        Map<String, Object> summary = new HashMap<>();
        summary.put("messageId", new MessageBoundary().getMessageId());
        summary.put("publishedTimestamp", publishedTimestamp);
        summary.put("messageType", "monthlyConsumptionSummary");
        summary.put("summary", "Your house consumed " + totalConsumption + " W/h in " + Month.of(date.getMonth()) + " " + date.getYear());
        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("consumption", totalConsumption);
        summary.put("messageDetails", messageDetails);

        // Return the summary message in a Flux
        return Mono.just(new MessageBoundary(summary)).log();
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

    public Mono<Float> calculateTotalLiveConsumption () {
        return deviceNotificationService.getAllDevicesNotificationMessages()
                .filter(device -> device.getStatus().isOn()) // Filter devices with status = true
                .map(d -> d.getStatus().getCurrentPowerInWatts()) // Map each device to its consumption
                .reduce(0.0f,Float::sum); // Sum up all the consumptions
    }

    public Mono<Float> calculateConsumptionForDay(LocalDateTime date) {
        LocalDateTime startDate = date.atStartOfDay()
                .withHour(0)
                .withMinute(1)
                .withSecond(0);

        LocalDateTime endDate = date.atTime(23, 59, 59);

        List <DeviceEntity> devices = this.DeviceNotificationRepository
                .findAllByRegistrationTimestampAfterAndLastUpdateTimestampBefore
                        (startDate,endDate); //need to check

        Float total = devices.stream()
                .map(d -> d.getStatus().getCurrentPowerInWatts()* d.getTotalActiveTime()) // Map each device to its consumption
                .reduce(0.0f, Float::sum);

        Float totalOn = devices.stream().
                filter(device -> device.getStatus().isOn()) // Filter devices with status = true
                .map(d -> d.getManufacturerPowerInWatts()* (new Date().getTime() - (float)d.getLastUpdateTimestamp().getTime())/360000)
                .reduce(0.0f,Float::sum);

        return Mono.just(totalOn+total);
    }

    public float calculateMonthlyConsumptionForDate(Date date) {

    }
}
