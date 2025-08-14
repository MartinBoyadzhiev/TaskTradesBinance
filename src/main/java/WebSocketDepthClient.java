import com.google.gson.Gson;
import dto.BookUpdate;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketDepthClient extends WebSocketClient {

    private LinkedBlockingQueue<BookUpdate> buffer;
    private final Gson gson = new Gson();
    private final AtomicBoolean hasNewUpdates;

    public WebSocketDepthClient(String wsUrl, LinkedBlockingQueue<BookUpdate> buffer, AtomicBoolean hasNewUpdates) throws URISyntaxException {
        super(new URI(wsUrl));
        this.buffer = buffer;
        this.hasNewUpdates = hasNewUpdates;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected");
    }

    @Override
    public void onMessage(String s) {
        BookUpdate update = gson.fromJson(s, BookUpdate.class);
        buffer.offer(update);
        this.hasNewUpdates.set(true);
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