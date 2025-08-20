package dto;

import java.util.List;

public record OrderBookSnapshot(
        long lastUpdateId,
        List<List<String>> bids,
        List<List<String>> asks
) {}
