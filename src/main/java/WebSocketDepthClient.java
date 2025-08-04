import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketDepthClient extends WebSocketClient {

    public WebSocketDepthClient(String wsUrl) throws URISyntaxException {
        super(new URI(wsUrl));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected");
    }

    @Override
    public void onMessage(String s) {
        System.out.println("Message " + s);
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
