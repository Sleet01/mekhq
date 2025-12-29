package mekhq.campaign.espionage;

public interface IResultEvaluator {

    public boolean checkAchieved(SphereOfInfluence soi);

    public void apply(SphereOfInfluence soi);

}
