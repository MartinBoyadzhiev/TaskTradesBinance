package handles;

import dto.BookUpdate;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueHandle {
    private final String streamName;
    private final AtomicBoolean hasData;
    private final LinkedBlockingQueue<BookUpdate> streamQueue;

    public QueueHandle(String streamName, LinkedBlockingQueue<BookUpdate> streamQueue) {
        this.streamName = streamName;
        this.hasData = new AtomicBoolean();
        this.streamQueue = streamQueue;
    }

    public String getStreamName() {
        return streamName;
    }

    public AtomicBoolean getHasData() {
        return hasData;
    }

    public LinkedBlockingQueue<BookUpdate> getStreamQueue() {
        return streamQueue;
    }
}
