package mekhq.campaign.espionage;

import megamek.common.annotations.Nullable;
import megamek.logging.MMLogger;

import java.util.ArrayList;
import java.util.HashMap;

public class SphereOfInfluence {
    private static final MMLogger LOGGER = MMLogger.create(SphereOfInfluence.class);

    // Use Player / Bot IDs as keys for now
    private HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap;
    private ArrayList<IntelItem> items;
    private HashMap<Integer, ArrayList<IntelEvent>> eventsMap;

    public SphereOfInfluence() {
         this(new HashMap<>(), new ArrayList<>(), new HashMap<>());
    }

    public SphereOfInfluence(
          HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap,
          ArrayList<IntelItem> items,
          HashMap<Integer, ArrayList<IntelEvent>> eventsMap
    ) {
        this.actorsRatingsMap = actorsRatingsMap;
    }

    public @Nullable IntelRating getActorRatingForFoe(int actorId, int foeId) {
        if (actorsRatingsMap.containsKey(actorId)) {
            return actorsRatingsMap.get(actorId).get(foeId);
        }
        return null;
    }

    public void addActorRatingForFoe(int actorId, int foeId) {
        setActorRatingForFoe(actorId, foeId, new IntelRating());
    }

    public void addActorRatingForFoe(int actorId, int foeId, int rating ) {
        setActorRatingForFoe(actorId, foeId, new IntelRating(rating));
    }

    public void setActorRatingForFoe(int actorId, int foeId, IntelRating ratingForFoe) {
        HashMap<Integer, IntelRating> newRating = (actorsRatingsMap.containsKey(actorId))
                                            ? actorsRatingsMap.get(actorId) : new HashMap<>();

        newRating.put(foeId, ratingForFoe);
        actorsRatingsMap.put(actorId, newRating);
    }

    public boolean update() {
        return false;
    }

}
