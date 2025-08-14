import com.google.gson.Gson;
import dto.BookUpdate;
import dto.OrderBookSnapshot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class LocalOrderBook {
    private final String SNAPSHOT_URL = "https://api.binance.com/api/v3/depth?symbol=BTCUSDT&limit=1000";
    private long lastUpdateID;
    private final TreeMap<Double, Double> asks = new TreeMap<>();
    private final TreeMap<Double, Double> bids = new TreeMap<>(Collections.reverseOrder());
    private final Gson gson = new Gson();

    public LocalOrderBook() {
    }

    public long getLastUpdateID() {
        return lastUpdateID;
    }

    public double midPrice() {
        return (this.bids.firstKey() + this.asks.firstKey()) / 2;
    }

    public boolean update(BookUpdate update) {
        if (update.U() <= lastUpdateID && update.u() >= lastUpdateID) {
            applyUpdates(asks, update.a());
            applyUpdates(bids, update.b());
            lastUpdateID = update.u();
            return true;
        } else if (lastUpdateID < update.U()) {
            applyUpdates(asks, update.a());
            applyUpdates(bids, update.b());
            lastUpdateID = update.u();
            return true;
        }
        System.out.println("_______________________________________________________________");
        return false;
    }

    public void syncOrderBook() {
        clearBook();
        OrderBookSnapshot snapshot = this.getSnapshot();

        this.lastUpdateID = snapshot.lastUpdateID();

        snapshot.asks().forEach(ask -> asks.put(
                Double.parseDouble(ask.getFirst()),
                Double.parseDouble(ask.get(1))
        ));

        snapshot.bids().forEach(bid -> bids.put(
                Double.parseDouble(bid.getFirst()),
                Double.parseDouble(bid.get(1))
        ));
    }

    private OrderBookSnapshot getSnapshot() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(SNAPSHOT_URL))
                .GET()
                .build();

        HttpResponse<String> httpResponse;
        try {
            httpResponse = httpClient.
                    send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        return gson.fromJson(httpResponse.body(), OrderBookSnapshot.class);
    }

    private void applyUpdates(TreeMap<Double, Double> target, List<List<String>> updateList) {
        for (List<String> level : updateList) {
            double price = Double.parseDouble(level.getFirst());
            double volume = Double.parseDouble(level.getLast());
            if (volume == 0) {
                target.remove(price);
            } else {
                target.put(price, volume);
            }
        }
    }

    public double calculateVWAPAsks(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Incorrect input amount for VWAP in asks.");
        }

        double originalAmount = amount;
        double sum = 0;

        for (Map.Entry<Double, Double> entry : asks.entrySet()) {
            double price = entry.getKey();
            double volume = entry.getValue();

            if (volume >= amount) {
                sum += amount * price;
                amount = 0;
                break;
            } else {
                sum += volume * price;
                amount -= volume;
            }
        }

        if (amount > 0 ) {
            return Double.NaN;
        }

        return sum / originalAmount;
    }

    public double calculateVWAPBids(double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Incorrect input amount for VWAP in bids.");
        }

        double originalAmount = amount;
        double sum = 0;

        for (Map.Entry<Double, Double> entry : bids.entrySet()) {
            double price = entry.getKey();
            double volume = entry.getValue();

            if (volume >= amount) {
                sum += amount * price;
                amount = 0;
                break;
            } else {
                sum += volume * price;
                amount -= volume;
            }
        }

        if (amount > 0 ) {

            return Double.NaN;
        }

        return sum / originalAmount;
    }

    private void clearBook() {
        this.asks.clear();
        this.bids.clear();
    }
}