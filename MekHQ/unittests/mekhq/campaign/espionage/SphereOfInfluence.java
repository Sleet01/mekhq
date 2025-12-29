package mekhq.campaign.espionage;

import java.util.HashMap;

public class SphereOfInfluence {

    // Use Player / Bot IDs as keys for now
    private HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap;

    public SphereOfInfluence() {
         this(new HashMap<>());
    }

    public SphereOfInfluence(HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap) {
        this.actorsRatingsMap = actorsRatingsMap;
    }
}
