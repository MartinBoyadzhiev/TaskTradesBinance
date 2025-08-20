import com.google.gson.Gson;
import dto.BookUpdate;
import dto.OrderBookSnapshot;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.TreeMap;

public class BookHandler {
    private final String streamName;
    private final LocalOrderBook localBook;
    private final Gson gson = new Gson();

    public BookHandler(String streamName) {
        this.localBook = new LocalOrderBook(streamName);
        this.streamName = streamName;
    }

    public LocalOrderBook getLocalBook() {
        return localBook;
    }

    public void update(BookUpdate updateData) {
        if (updateData.U() > this.localBook.getLastUpdateID() + 1) {
            System.out.println("Last updateData ID: " + this.localBook.getLastUpdateID() + " U: " + updateData.U() + " u: " + updateData.u());
            syncOrderBook();
        }

        if (updateData.u() <= localBook.getLastUpdateID()) {
            System.out.println("Skipping U: " + updateData.U() + " u: " + updateData.u());
            return;
        }

        applyUpdates(localBook.getAsks(), updateData.a());
        applyUpdates(localBook.getBids(), updateData.b());
        localBook.setLastUpdateID(updateData.u());
    }

    public void syncOrderBook() {
        clearBook();
        OrderBookSnapshot snapshot = this.getSnapshot();

        this.localBook.setLastUpdateID(snapshot.lastUpdateId());

        snapshot.asks().forEach(ask -> this.localBook.getAsks().put(
                Double.parseDouble(ask.getFirst()),
                Double.parseDouble(ask.get(1))
        ));

        snapshot.bids().forEach(bid -> this.localBook.getBids().put(
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
        this.localBook.setLastUpdateID(-1);
        this.localBook.getAsks().clear();
        this.localBook.getBids().clear();
    }

    private String formatSnapshotURL() {
        return String.format(System.getenv("REST_ENDPOINT_TEMPLATE"),
                this.streamName.toUpperCase(), System.getenv("SNAPSHOT_LEVELS"));
    }
}
