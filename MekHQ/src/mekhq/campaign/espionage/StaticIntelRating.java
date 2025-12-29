package mekhq.campaign.espionage;

/**
 * This class represents an IntelRating that does not age or improve.
 * However, its levels may be changed in some circumstances.
 */
public class StaticIntelRating extends IntelRating {

    public StaticIntelRating() {
        super();
    }

    public StaticIntelRating(int rating) {
        super(rating);
    }

    public StaticIntelRating(int forcesLevel, int positionLevel, int logisticsLevel,
          int personnelLevel, int commsLevel, int financialLevel, int localLevel, int counterLevel) {
        super(
              forcesLevel,
              positionLevel,
              logisticsLevel,
              personnelLevel,
              commsLevel,
              financialLevel,
              localLevel,
              counterLevel
        );
    }

    @Override
    public void ageAllIntelByOne() {
        // pass
    }

    @Override
    public void setAnIntelToLevel(String name, int level) {
        // pass
    }

    @Override
    public void setAllIntelToLevel(int level) {
        // pass
    }

    @Override
    public void ageAllIntel(int delta) {
        // pass
    }

    @Override
    public void ageAnIntel(String name, int delta) {
        // pass
    }

    @Override
    public void improveAnIntel(String name, int delta) {
        // pass
    }
}

