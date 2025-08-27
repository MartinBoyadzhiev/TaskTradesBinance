package handles;

import dto.BookUpdate;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueHandle {

    private final String pairName;
    private final LinkedBlockingQueue<BookUpdate> streamQueue;

    public QueueHandle(String pairName, LinkedBlockingQueue<BookUpdate> streamQueue) {
        this.pairName = pairName;
        this.streamQueue = streamQueue;
    }

    public String getPairName() {
        return pairName;
    }

    public LinkedBlockingQueue<BookUpdate> getStreamQueue() {
        return streamQueue;
    }
}