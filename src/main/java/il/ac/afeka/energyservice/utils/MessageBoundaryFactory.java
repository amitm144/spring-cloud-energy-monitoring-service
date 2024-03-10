package il.ac.afeka.energyservice.utils;

import il.ac.afeka.energyservice.boundaries.ExternalReferenceBoundary;
import il.ac.afeka.energyservice.boundaries.HistoricalConsumptionBoundary;
import il.ac.afeka.energyservice.boundaries.MessageBoundary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class MessageBoundaryFactory {
    private static MessageBoundaryFactory _instance;

    @Value("${spring.application.id}")
    private String EXT_SERVICE_ID;
    @Value("${spring.application.name}")
    private String SERVICE_NAME;

    private MessageBoundaryFactory() {}

    public static MessageBoundaryFactory get() {
        if (_instance == null) {
            _instance = new MessageBoundaryFactory();
        }
        return _instance;
    }

    public MessageBoundary generateOverCurrentWarning(String deviceId, String deviceType, float currentConsumption) {
        MessageBoundary overCurrentWarning = new MessageBoundary();

        overCurrentWarning.setPublishedTimestamp(LocalDateTime.now());
        overCurrentWarning.setMessageType("overcurrentWarning");
        overCurrentWarning.setSummary("device " + deviceId + " is over consuming");

        overCurrentWarning.setExternalReferences(createDefaultExternalReferences());;

        HashMap<String, Object> details = new HashMap<>();
        details.put("deviceId", deviceId);
        details.put("deviceType", deviceType);
        details.put("currentConsumption", currentConsumption);
        overCurrentWarning.setMessageDetails(details);

        return overCurrentWarning;
    }

    public MessageBoundary generateOverConsumptionWarning(float currentConsumption) {
        MessageBoundary overConsumptionWarning = new MessageBoundary();

        overConsumptionWarning.setPublishedTimestamp(LocalDateTime.now());
        overConsumptionWarning.setMessageType("consumptionWarning");
        overConsumptionWarning.setSummary("You have reached your daily consumption limit");

        overConsumptionWarning.setExternalReferences(createDefaultExternalReferences());;

        HashMap<String, Object> details = new HashMap<>();
        details.put("currentConsumption", currentConsumption);
        overConsumptionWarning.setMessageDetails(details);

        return overConsumptionWarning;
    }

    public MessageBoundary generateLiveConsumptionSummary(float liveConsumption) {
        MessageBoundary liveSummary = new MessageBoundary();

        liveSummary.setMessageType("liveConsumptionSummary");
        liveSummary.setPublishedTimestamp(LocalDateTime.now());
        liveSummary.setSummary("Your house is currently consuming " + liveConsumption + "W");

        liveSummary.setExternalReferences(createDefaultExternalReferences());

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("currentConsumption:", liveConsumption);
        liveSummary.setMessageDetails(messageDetails);
        liveSummary.setMessageDetails(messageDetails);

        return liveSummary;
    }

    public MessageBoundary generateDailyConsumptionSummary(float totalConsumption, float expectedBill) {
        MessageBoundary dailySummary = new MessageBoundary();

        dailySummary.setMessageType("dailyConsumptionSummary");
        dailySummary.setPublishedTimestamp(LocalDateTime.now());
        dailySummary.setSummary("Your total power consumption for today is " + totalConsumption + "W");

        dailySummary.setExternalReferences(createDefaultExternalReferences());

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("totalConsumption:", totalConsumption);
        messageDetails.put("expectedBill:", expectedBill);
        dailySummary.setMessageDetails(messageDetails);

        return dailySummary;
    }

    public MessageBoundary generateMonthlyConsumptionSummary(float totalConsumption, float expectedBill, LocalDate date,
                                                             List<HistoricalConsumptionBoundary> previousConsumptions) {
        MessageBoundary monthlySummary = new MessageBoundary();

        monthlySummary.setMessageType("monthlyConsumptionSummary");
        monthlySummary.setPublishedTimestamp(LocalDateTime.now());
        monthlySummary.setSummary("Your total power consumption for " + date.getMonth() + " is " + totalConsumption + "W");

        monthlySummary.setExternalReferences(createDefaultExternalReferences());

        Map<String, Object> messageDetails = new HashMap<>();
        messageDetails.put("totalConsumption", totalConsumption);
        messageDetails.put("expectedBill", expectedBill);
        messageDetails.put("historicalConsumption:",previousConsumptions);
        monthlySummary.setMessageDetails(messageDetails);

        return monthlySummary;
    }

    private Set<ExternalReferenceBoundary> createDefaultExternalReferences() {
        ExternalReferenceBoundary externalReference = new ExternalReferenceBoundary();
        externalReference.setExternalServiceId(EXT_SERVICE_ID);
        externalReference.setService(SERVICE_NAME);

        Set<ExternalReferenceBoundary> refs = new HashSet<>();
        refs.add(externalReference);

        return refs;
    }
}
