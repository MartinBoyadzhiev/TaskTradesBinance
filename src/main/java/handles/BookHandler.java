package handles;

import com.google.gson.Gson;
import dto.BookUpdate;
import dto.OrderBookSnapshot;
import dto.BinanceBookUpdate;
import dto.OrderLevel;
import enums.EnvVar;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.TreeMap;

public class BookHandler {

    private final String pairName;
    private final LocalOrderBook book;
    private final Gson gson = new Gson();

    public BookHandler(String pairName) {
        this.book = new LocalOrderBook(pairName);
        this.pairName = pairName;
    }

    public LocalOrderBook getBook() {
        return this.book;
    }

    public void handleUpdateData(BookUpdate updateData) {
        if (updateData instanceof BinanceBookUpdate update) {
            if (update.getFirstUpdate() > this.book.getLastUpdateID() + 1) {
                System.out.println("Last updateData ID: " + this.book.getLastUpdateID() + " U: " + update.getFirstUpdate() + " u: " + update.getLastUpdate());
                onSnapshot();
            }

            if (update.getLastUpdate() <= book.getLastUpdateID()) {
                System.out.println("Skipping U: " + update.getFirstUpdate() + " u: " + update.getLastUpdate());
                return;
            }
            onUpdate(update);
        }
    }

    private void onUpdate(BinanceBookUpdate updateData) {
        applyUpdates(book.getAsks(), updateData.getAsks());
        applyUpdates(book.getBids(), updateData.getBids());
        this.book.setLastUpdateID(updateData.getLastUpdate());
    }

    private void onSnapshot() {
        clearBook();
        OrderBookSnapshot snapshot = getSnapshot();

        this.book.setLastUpdateID(snapshot.lastUpdateId());

        snapshot.asks().forEach(ask -> this.book.getAsks().put(
                Double.parseDouble(ask.getFirst()),
                Double.parseDouble(ask.get(1))
        ));

        snapshot.bids().forEach(bid -> this.book.getBids().put(
                Double.parseDouble(bid.getFirst()),
                Double.parseDouble(bid.get(1))
        ));
    }

    private OrderBookSnapshot getSnapshot() {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(formatSnapshotURL()))
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

    private void applyUpdates(TreeMap<Double, Double> target, List<OrderLevel> updateList) {
        for (OrderLevel level : updateList) {
            double price = level.price();
            double qty = level.qty();
            if (qty == 0) {
                target.remove(price);
            } else {
                target.put(price, qty);
            }
        }
    }

    private void clearBook() {
        this.book.setLastUpdateID(-1);
        this.book.getAsks().clear();
        this.book.getBids().clear();
    }

    private String formatSnapshotURL() {
        return String.format(EnvVar.REST_ENDPOINT_TEMPLATE.get(), this.pairName.toUpperCase(), EnvVar.SNAPSHOT_LEVELS.getInt());
    }
}
