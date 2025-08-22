package handles;

import com.google.gson.Gson;
import dto.BookUpdate;
import dto.OrderBookSnapshot;
import enums.EnvVar;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.TreeMap;

public class BookHandle {

    private final String pairName;
    private final LocalOrderBook book;
    private final Gson gson = new Gson();

    public BookHandle(String pairName) {
        this.book = new LocalOrderBook(pairName);
        this.pairName = pairName;
    }

    public LocalOrderBook getBook() {
        return this.book;
    }

    public void handleUpdateData(BookUpdate updateData) {
        if (updateData.U() > this.book.getLastUpdateID() + 1) {
            System.out.println("Last updateData ID: " + this.book.getLastUpdateID() + " U: " + updateData.U() + " u: " + updateData.u());
            onSnapshot();
        }

        if (updateData.u() <= book.getLastUpdateID()) {
            System.out.println("Skipping U: " + updateData.U() + " u: " + updateData.u());
            return;
        }
        onUpdate(updateData);
    }

    private void onUpdate(BookUpdate updateData) {
        applyUpdates(book.getAsks(), updateData.a());
        applyUpdates(book.getBids(), updateData.b());
        this.book.setLastUpdateID(updateData.u());
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

    private void clearBook() {
        this.book.setLastUpdateID(-1);
        this.book.getAsks().clear();
        this.book.getBids().clear();
    }

    private String formatSnapshotURL() {
        return String.format(EnvVar.REST_ENDPOINT_TEMPLATE.get(),
                this.pairName.toUpperCase(), EnvVar.SNAPSHOT_LEVELS.getInt());
    }
}
