import com.google.gson.Gson;
import common.dto.BookUpdate;
import common.dto.OrderLevel;
import dto_binance.*;
import config.Constants;
import common.QueueHandle;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BinanceWebSocketConnector extends WebSocketClient {

    private final HashMap<String, Map<String, QueueHandle>> queueHandlerMapPerExchange;
    private final Logger logger = LoggerFactory.getLogger(BinanceWebSocketConnector.class);
    private final Gson gson = new Gson();

    public BinanceWebSocketConnector(HashMap<String, Map<String, QueueHandle>> queueHandlerMap) throws URISyntaxException {
        super(new URI(formatWebSocketURL()));
        this.queueHandlerMapPerExchange = queueHandlerMap;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("Successful connection");
    }

    @Override
    public void onMessage(String s) {
        StreamMessage message = gson.fromJson(s, StreamMessage.class);
        StreamData data = message.data();
        BookUpdate update = new BinanceBookUpdate(data.U(), data.u());
        parseLevels(data.a(), update.getAsks());
        parseLevels(data.b(), update.getBids());

        Map<String, QueueHandle> queueHandleMap = queueHandlerMapPerExchange.get("binance");
        queueHandleMap.computeIfPresent(message.stream().split("@")[0], (k, handler) -> {
            handler.getStreamQueue().offer(update);
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
        List<String> subscriptionPairs = Arrays.stream(Constants.BINANCE_PAIR.split(",")).toList();
        StringBuilder sb = new StringBuilder("wss://stream.binance.com:9443/stream?streams=");
        String formattedPairs = subscriptionPairs.stream().map(s -> s + "@depth@100ms")
                .collect(Collectors.joining("/"));
        sb.append(formattedPairs);
        return sb.toString();
    }

    private void parseLevels(List<List<String>> data, List<OrderLevel> levels) {
        for (List<String> level : data) {
            double price = Double.parseDouble(level.getFirst());
            double qty = Double.parseDouble(level.get(1));
            levels.add(new OrderLevel(price, qty));
        }
    }
}