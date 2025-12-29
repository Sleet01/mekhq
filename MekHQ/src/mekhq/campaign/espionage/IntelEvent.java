package mekhq.campaign.espionage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class IntelEvent {
    // Universal "Location" doesn't exist yet, use a List for now.
    private ArrayList<String> locationParts;
    private LocalDate startDate;
    private LocalDate endDate;
    private ArrayList<Integer> particapantIds;
    private IntelItem intelItem;


}
