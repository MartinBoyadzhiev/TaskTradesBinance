import dto.BookUpdate;

import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainConnectionTest {

    private static final String WS_URL = "wss://stream.binance.com:9443/ws/btcusdt@depth@100ms";

    public static void main(String[] args) throws URISyntaxException {
        AtomicBoolean hasNewUpdates = new AtomicBoolean(false);
//        Buffer
        LinkedBlockingQueue<BookUpdate> buffer = new LinkedBlockingQueue<>();
//        WebSocket Stream
        WebSocketDepthClient wsClient = new WebSocketDepthClient(WS_URL, buffer, hasNewUpdates);
        wsClient.connect();

        BookConsumer bookConsumer = new BookConsumer(buffer, hasNewUpdates);
        bookConsumer.start();
    }
}