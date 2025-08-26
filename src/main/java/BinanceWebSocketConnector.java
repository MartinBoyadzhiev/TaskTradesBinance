import com.google.gson.Gson;
import dto.StreamData;
import dto.BookUpdate;
import dto.StreamMessage;
import dto.BinanceBookUpdate;
import enums.EnvVar;
import handles.QueueHandle;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class BinanceWebSocketConnector extends WebSocketClient {

    private final ConcurrentHashMap<String, QueueHandle> queueHandlerMap;
    private final Logger logger = LoggerFactory.getLogger(BinanceWebSocketConnector.class);
    private final Gson gson = new Gson();

    public BinanceWebSocketConnector(ConcurrentHashMap<String, QueueHandle> queueHandlerMap) throws URISyntaxException {
        super(new URI(formatWebSocketURL()));
        this.queueHandlerMap = queueHandlerMap;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("Successful connection");
    }

    @Override
    public void onMessage(String s) {
        StreamMessage message = gson.fromJson(s, StreamMessage.class);
        StreamData data = message.data();

        BookUpdate update = new BinanceBookUpdate(data.s(), data.e(), data.E(), data.U(), data.u(), data.a(), data.b());

        this.queueHandlerMap.computeIfPresent(message.stream().split("@")[0], (k, handler) -> {
            handler.getStreamQueue().offer(update);
            handler.getHasData().set(true);
            return handler;
        });
    }

    @Override
    public void onClose(int code, String reason, boolean b) {
        //        TODO Reconnection logic
        logger.warn("WS closed: code={} reason={} remote={}", code, reason, b);
    }

    @Override
    public void onError(Exception e) {
        //        TODO Reconnection logic
        logger.error("WebSocket error on stream: {}", e.getMessage(), e);
    }

    private static String formatWebSocketURL() {
        List<String> subscriptionPairs = Arrays.stream(EnvVar.BINANCE_PAIR.get().split(",")).toList();
        StringBuilder sb = new StringBuilder("wss://stream.binance.com:9443/stream?streams=");
        sb.append(subscriptionPairs.stream().map(s -> s + "@depth@100ms")
                .collect(Collectors.joining("/")));
        return sb.toString();
    }
}