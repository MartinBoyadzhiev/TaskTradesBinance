import dto.BookUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookConsumer extends Thread {
    private final LinkedBlockingQueue<BookUpdate> buffer;
    private final AtomicBoolean hasNewUpdates;
    private LocalOrderBook orderBook;
    private long lastCalculationTime = 0;
    private static final Logger logger = LoggerFactory.getLogger(BookConsumer.class);

    public BookConsumer(LinkedBlockingQueue<BookUpdate> buffer, AtomicBoolean hasNewUpdates) {
        this.buffer = buffer;
        this.hasNewUpdates = hasNewUpdates;
    }

    @Override
    public void run() {
        this.orderBook = new LocalOrderBook();
        waitForDataAndSyncBook();

        while (true) {
            try {
                BookUpdate bookUpdate = buffer.take();
                if (bookUpdate.u() <= orderBook.getLastUpdateID()) {
                    continue;
                }
                boolean update = orderBook.update(bookUpdate);
                if (!update) {
                    waitForDataAndSyncBook();
                }
                doCalculations();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void doCalculations() {
        if (System.currentTimeMillis() - lastCalculationTime > 1000) {
            logger.info("Mid price is -> {}", orderBook.midPrice());
            lastCalculationTime = System.currentTimeMillis();
        }
    }

    private void waitForDataAndSyncBook() {
        this.hasNewUpdates.set(false);

        while (!hasNewUpdates.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        this.orderBook.syncOrderBook();
    }
}
