import java.util.*;

public class LocalOrderBook {
    private long lastUpdateID;
    private final TreeMap<Double, Double> bids = new TreeMap<>(Collections.reverseOrder());
    private final TreeMap<Double, Double> asks = new TreeMap<>();

    public LocalOrderBook() {}

    public long getLastUpdateID() {
        return lastUpdateID;
    }

    public Map<Double, Double> getBids() {
        return bids;
    }

    public Map<Double, Double> getAsks() {
        return asks;
    }

    public void setLastUpdateID(long lastUpdateID) {
        this.lastUpdateID = lastUpdateID;
    }

    public double midPrice() {
        return (this.bids.firstKey() + this.asks.firstKey()) / 2;
    }
}