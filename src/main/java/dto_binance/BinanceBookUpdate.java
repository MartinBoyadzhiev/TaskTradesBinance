package dto_binance;

import common.dto.BookUpdate;

public class BinanceBookUpdate extends BookUpdate {

    private final long firstUpdate;
    private final long lastUpdate;

    public BinanceBookUpdate(long firstUpdate, long lastUpdate) {
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