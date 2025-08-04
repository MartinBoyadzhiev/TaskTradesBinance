import com.google.gson.Gson;
import dto.OrderBookSnapshot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainConnectionTest {
    public static void main(String[] args) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newHttpClient();

        String url = "https://api.binance.com/api/v3/depth?symbol=BTCUSDT&limit=100";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> httpResponse = httpClient.
                send(httpRequest, HttpResponse.BodyHandlers.ofString());

        Gson gson = new Gson();
        OrderBookSnapshot snapshot = gson.fromJson(httpResponse.body(), OrderBookSnapshot.class);

        LocalOrderBook orderBook = new LocalOrderBook();
        orderBook.setLastUpdateID(snapshot.lastUpdateID());

        snapshot.asks().forEach(ask -> {
            orderBook.getAsks().put(
                    Double.parseDouble(ask.get(0)),
                    Double.parseDouble(ask.get(1))
            );
        });

        snapshot.bids().forEach(bid -> {
            orderBook.getBids().put(
                    Double.parseDouble(bid.get(0)),
                    Double.parseDouble(bid.get(1))
            );
        });

        System.out.println();

    }
}
