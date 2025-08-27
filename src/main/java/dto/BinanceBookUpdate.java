package dto;

public class BinanceBookUpdate extends BookUpdate {

    private final long firstUpdate;
    private final long lastUpdate;

    public BinanceBookUpdate(String pairName, long firstUpdate, long lastUpdate) {
        super(pairName);
        this.firstUpdate = firstUpdate;
        this.lastUpdate = lastUpdate;
    }

    public long getFirstUpdate() {
        return firstUpdate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }
}