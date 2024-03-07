package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.repositories.EnergyMonitoringRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class EnergyConsumptionService implements EnergyConsumptionsService {
    private final EnergyMonitoringRepository energyMonitoringRepository;

    public EnergyConsumptionService(EnergyMonitoringRepository energyMonitoringRepository) {
        this.energyMonitoringRepository = energyMonitoringRepository;
    }

    @Override
    public Mono<MessageBoundary> getLiveConsumption() {
        return Mono.just(createDemoLiveConsumptionSummeryBoundary());
    }

    @Override
    public Mono<Void> handleDeviceEvent(MessageBoundary message) {
        return null; // Fire and Forget returns nothing
    }

    @Override
    public Flux<MessageBoundary> getLiveConsumptionSummery() {
        MessageBoundary liveConsumptionSummary = createDemoLiveConsumptionSummeryBoundary();
        return Flux.just(liveConsumptionSummary);
    }

    @Override
    public Flux<MessageBoundary> getConsumptionSummaryByDay(Date day) {
        MessageBoundary summary = createDemoLiveConsumptionSummeryBoundary();
        summary.setPublishedTimestamp(day);
        summary.setMessageType("consumptionSummary");

        return Flux.just(summary);
    }

    @Override
    public Flux<MessageBoundary> getConsumptionSummaryByMonth(Date date) {
        MessageBoundary summary = createDemoLiveConsumptionSummeryBoundary();
        summary.setPublishedTimestamp(date);
        summary.setMessageType("consumptionSummary");

        return Flux.just(summary);
    }

    private MessageBoundary createDemoLiveConsumptionSummeryBoundary() {
        // Create a new MessageBoundary object with the demo data
        MessageBoundary liveConsumptionSummary = new MessageBoundary();
        liveConsumptionSummary.setMessageId("12345");
        liveConsumptionSummary.setPublishedTimestamp(new Date());
        liveConsumptionSummary.setMessageType("liveConsumptionSummary");
        liveConsumptionSummary.setSummary("Your house is currently consuming 5kW/h.");

        // Set external references
        ExternalReferenceBoundary externalReference = new ExternalReferenceBoundary();
        externalReference.setExternalServiceId("0");
        externalReference.setService("EnergyServiceProvider");
        Set<ExternalReferenceBoundary> refs = new HashSet<>();
        refs.add(externalReference);
        liveConsumptionSummary.setExternalReferences(refs);

        // Set message details including consumption by room
        HashMap<String, Object> details = new HashMap<>();
        details.put("houseId", -1);
        double totalConsumption = 5.0;
        details.put("Consumption", totalConsumption);

        // Prepare room consumption details
        List<HashMap<String, Object>> roomsConsumption = new ArrayList<>();
        Random random = new Random();
        double remainingConsumption = totalConsumption;

        // Define room names
        String[] roomNames = {"Living Room", "Kitchen", "Bedroom", "Bathroom"};
        for (int i = 0; i < roomNames.length; i++) {
            HashMap<String, Object> room = new HashMap<>();
            room.put("roomId", "room" + (i + 1));
            room.put("roomName", roomNames[i]);

            // Randomize consumption for each room, ensuring the last room adjusts to match the total
            double consumption;
            if (i < roomNames.length - 1) {
                consumption = Math.round(random.nextDouble() * (remainingConsumption - (roomNames.length - i - 1)) * 100.0) / 100.0;
                remainingConsumption -= consumption;
            } else {
                consumption = Math.round(remainingConsumption * 100.0) / 100.0; // Adjust the last room's consumption to match exactly
            }

            room.put("consumption", consumption);
            roomsConsumption.add(room);
        }

        // Add the list of rooms to the details HashMap
        details.put("consumptionByRoom", roomsConsumption);

        // Set the details in the liveConsumptionSummary object
        liveConsumptionSummary.setMessageDetails(details);
        return liveConsumptionSummary;
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
