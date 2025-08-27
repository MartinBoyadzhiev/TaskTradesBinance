package dto;

import java.util.ArrayList;
import java.util.List;

public abstract class BookUpdate {
    private final String pairName;
    private final List<OrderLevel> asks;
    private final List<OrderLevel> bids;

    public BookUpdate(String pairName) {
        this.pairName = pairName;
        this.asks = new ArrayList<>();
        this.bids = new ArrayList<>();
    }

    public List<OrderLevel> getAsks() {
        return asks;
    }

    public List<OrderLevel> getBids() {
        return bids;
    }
}
