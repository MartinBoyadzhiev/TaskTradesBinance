package dto;

import java.util.List;

public class BinanceBookUpdate extends BookUpdate {

    private final String eventType;
    private final long eventTime;
    private final long firstUpdate;
    private final long lastUpdate;
    private final List<List<String>> bids;
    private final List<List<String>> asks;

    public BinanceBookUpdate(String pairName, String eventType, long eventTime, long firstUpdate, long lastUpdate, List<List<String>> asks, List<List<String>> bids) {
        setPairName(pairName);
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.firstUpdate = firstUpdate;
        this.lastUpdate = lastUpdate;
        this.bids = bids;
        this.asks = asks;
    }

    public long getFirstUpdate() {
        return firstUpdate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public List<List<String>> getBids() {
        return bids;
    }

    public List<List<String>> getAsks() {
        return asks;
    }
}
