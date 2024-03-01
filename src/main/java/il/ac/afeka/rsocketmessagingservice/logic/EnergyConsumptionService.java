package il.ac.afeka.rsocketmessagingservice.logic;

import il.ac.afeka.rsocketmessagingservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.rsocketmessagingservice.boundaries.MessageBoundary;
import il.ac.afeka.rsocketmessagingservice.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnergyConsumptionService implements EnergyConsumptionsService {
    private final MessageRepository messageRepository;

    public EnergyConsumptionService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    @Override
    public Mono<MessageBoundary> createNewHouse(MessageBoundary message) {
        MessageBoundary liveConsumptionSummary = GetDemoLiveConsumptionSummeryBoundry();
        return Mono.just(liveConsumptionSummary);
    }

    private MessageBoundary GetDemoLiveConsumptionSummeryBoundry() {
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

    @Override
    public Mono<Void> HandleDeviceEvent(MessageBoundary message) {
        return null; // Fire and Forget returns nothing
    }

    @Override
    public Flux<MessageBoundary> GetCurrentConsumptionSummery(Flux<MessageBoundary> references) {
        MessageBoundary liveConsumptionSummary = GetDemoLiveConsumptionSummeryBoundry();
        return Flux.just(liveConsumptionSummary);
    }

    @Override
    public Flux<MessageBoundary> GetConsumptionSummery(Flux<MessageBoundary> references) {
        MessageBoundary liveConsumptionSummary = GetDemoLiveConsumptionSummeryBoundry();
        return Flux.just(liveConsumptionSummary);
    }
}
