import com.google.gson.Gson;
import dto.BookUpdate;
import dto.StreamMessage;
import handles.QueueHandle;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketDepthClient extends WebSocketClient {
    private final Gson gson = new Gson();
    private final ConcurrentHashMap<String, QueueHandle> queueHandlerMap;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketDepthClient.class);

    public WebSocketDepthClient(String wsUrl, ConcurrentHashMap<String, QueueHandle> queueHandlerMap) throws URISyntaxException {
        super(new URI(wsUrl));
        this.queueHandlerMap = queueHandlerMap;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        logger.info("Successful connection");
    }

    @Override
    public void onMessage(String s) {
        StreamMessage message = gson.fromJson(s, StreamMessage.class);
        BookUpdate update = message.data();

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
}