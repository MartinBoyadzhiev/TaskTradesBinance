import com.google.gson.Gson;
import dto.BookUpdate;
import dto.StreamMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketDepthClient extends WebSocketClient {

    private final LinkedBlockingQueue<BookUpdate> buffer;
    private final Gson gson = new Gson();
    private final ConcurrentHashMap<String, AtomicBoolean> flagHandler;

    public WebSocketDepthClient(String wsUrl,
                                LinkedBlockingQueue<BookUpdate> buffer,
                                ConcurrentHashMap<String, AtomicBoolean> flags) throws URISyntaxException {
        super(new URI(wsUrl));
        this.buffer = buffer;
        this.flagHandler = flags;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected");
    }

    @Override
    public void onMessage(String s) {
        StreamMessage message = gson.fromJson(s, StreamMessage.class);
        BookUpdate update = message.data();
        buffer.offer(update);
        this.flagHandler.computeIfPresent(message.stream().split("@")[0], (k, flag) -> {
            flag.set(true);
            return flag;
        });
    }

    @Override
    public void onClose(int code, String reason, boolean b) {
        System.out.println("Connection closed " + reason);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
}