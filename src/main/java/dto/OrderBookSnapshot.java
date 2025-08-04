package dto;

import java.util.List;

public record OrderBookSnapshot(
        long lastUpdateID,
        List<List<String>> bids,
        List<List<String>> asks
) {}
