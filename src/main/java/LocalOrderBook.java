import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class LocalOrderBook {
    private long lastUpdateID;
    private Map<Double, Double> bids = new TreeMap<>(Collections.reverseOrder());
    private Map<Double, Double> asks = new TreeMap<>();

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
}
