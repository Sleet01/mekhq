package mekhq.campaign.espionage;

import java.util.ArrayList;

public abstract class IntelOutcome implements IResultEvaluator {

    // If added here, objects will be awarded to "winner" of the IntelOutcome outcome.
    private ArrayList<Object> linkedObjects;
    private String title;
    private String description;
    private int beneficiaryId;

    public IntelOutcome() {
        this(new ArrayList<>());
    }

    public IntelOutcome(ArrayList<Object> linkedObjects) {
        this.linkedObjects = linkedObjects;
    }

    public ArrayList<Object> getLinkedObjects() {
        return linkedObjects;
    }

    public void setLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects = linkedObjects;
    }

    public void addLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects.addAll(linkedObjects);
    }

    public void addLinkedObject(Object linkedObject) {
        this.linkedObjects.add(linkedObject);
    }

    public void removeLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects.removeAll(linkedObjects);
    }

    public void removeLinkedObject(Object linkedObject) {
        this.linkedObjects.remove(linkedObject);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(int beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    // All subclasses must implement
    public boolean checkAchieved(SphereOfInfluence soi) {
        return false;
    }

    // All subclasses must implement
    public void apply(SphereOfInfluence soi) {
    }
}
