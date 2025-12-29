package mekhq.campaign.espionage;

import java.util.ArrayList;

public class IntelItem {

    public final static int UNSET_ID = -1;

    private int itemID;
    private int ownerId;
    private int possessorId;
    private String itemName;
    private String itemDescription;
    private boolean discovered = false;
    private boolean captured = false;
    private boolean deciphered = false;
    private boolean delivered = false;
    private boolean destroyed = false;
    private boolean escaped = false;

    // IntelResults will contain reward items
    private ArrayList<IntelOutcome> outcomes;

    public IntelItem() {
        this(UNSET_ID, UNSET_ID, UNSET_ID, "", "", new ArrayList<>());
    }

    public IntelItem(int itemID, int ownerID, int possessorId,
          String itemName, String itemDescription, ArrayList<IntelOutcome> outcomes) {
        this.itemID = itemID;
        this.ownerId = ownerID;
        this.possessorId = possessorId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.outcomes = outcomes;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getPossessorId() {
        return possessorId;
    }

    public void setPossessorId(int possessorId) {
        this.possessorId = possessorId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public boolean isDeciphered() {
        return deciphered;
    }

    public void setDeciphered(boolean deciphered) {
        this.deciphered = deciphered;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isEscaped() {
        return escaped;
    }

    public void setEscaped(boolean escaped) {
        this.escaped = escaped;
    }

    // SOI and/or Manager will likely directly edit these
    public ArrayList<IntelOutcome> getOutcomes() {
        return outcomes;
    }

    // SOI and/or Manager will likely edit these
    public void setOutcomes(ArrayList<IntelOutcome> outcomes) {
        this.outcomes = outcomes;
    }

    public String listOutcomeEntries() {
        StringBuilder resultList = new StringBuilder();
        for (IntelOutcome result : outcomes) {
            resultList.append(result.toString());
        }
        return resultList.toString();
    }

    public ArrayList<Object> listLinkedObjects() {
        ArrayList<Object> linkedObjects = new ArrayList<>();
        for (IntelOutcome outcome : outcomes) {
            linkedObjects.addAll(outcome.getLinkedObjects());
        }
        return linkedObjects;
    }
}
