import dto.BookUpdate;
import handles.QueueHandle;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class MainConnectionTest {

    public static void main(String[] args) throws URISyntaxException {
        List<String> subscriptionPairs = Arrays.stream(System.getenv("BINANCE_PAIRS").split(","))
                .toList();
        String webSocketURL = formatWsURL(subscriptionPairs);

        ConcurrentHashMap<String, QueueHandle> queueHandlerMap = initiateQueueMap(subscriptionPairs);

        WebSocketDepthClient wsClient = new WebSocketDepthClient(webSocketURL, queueHandlerMap);
        wsClient.setConnectionLostTimeout(0);
        wsClient.connect();

        BookConsumer bookConsumer = new BookConsumer(queueHandlerMap);
        bookConsumer.start();
    }

    private static ConcurrentHashMap<String, QueueHandle> initiateQueueMap(List<String> subscriptionPairs) {
        ConcurrentHashMap<String, QueueHandle> queueHandlerMap = new ConcurrentHashMap<>();

        for (String subscriptionPair : subscriptionPairs) {
            LinkedBlockingQueue<BookUpdate> queue = new LinkedBlockingQueue<>();
            QueueHandle handler = new QueueHandle(subscriptionPair, queue);
            queueHandlerMap.put(subscriptionPair, handler);
        }
        return queueHandlerMap;
    }

    private static String formatWsURL(List<String> subscriptionPairs) {
        StringBuilder sb = new StringBuilder("wss://stream.binance.com:9443/stream?streams=");
        sb.append(subscriptionPairs.stream().map(s -> s + "@depth@100ms")
                .collect(Collectors.joining("/")));
        return sb.toString();
    }
}