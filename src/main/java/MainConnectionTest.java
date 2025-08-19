import dto.BookUpdate;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class MainConnectionTest {

    public static void main(String[] args) throws URISyntaxException {

        List<String> subscriptionPairs = Arrays.stream(System.getenv("BINANCE_PAIRS").split(","))
                .toList();
        String webSocketURL = formatWsURL(subscriptionPairs);

        ConcurrentHashMap<String, AtomicBoolean> flagHandler = initFlags(subscriptionPairs);
//        Buffer
        LinkedBlockingQueue<BookUpdate> buffer = new LinkedBlockingQueue<>();
//        WebSocket Stream
        WebSocketDepthClient wsClient = new WebSocketDepthClient(webSocketURL, buffer, flagHandler);
        wsClient.setConnectionLostTimeout(0);
        wsClient.connect();

        BookConsumer bookConsumer = new BookConsumer(buffer, flagHandler);
        bookConsumer.start();
    }

    private static ConcurrentHashMap<String, AtomicBoolean> initFlags(List<String> subscriptionPairs) {
        ConcurrentHashMap<String, AtomicBoolean> flags = new ConcurrentHashMap<>();
        for (String subscriptionPair : subscriptionPairs) {
            flags.put(subscriptionPair, new AtomicBoolean());
        }
        return flags;
    }

    private static String formatWsURL(List<String> subscriptionPairs) {
        StringBuilder sb = new StringBuilder("wss://stream.binance.com:9443/stream?streams=");

        sb.append(subscriptionPairs.stream().map(s -> s + "@depth@100ms")
                .collect(Collectors.joining("/")));
        return sb.toString();
    }
}