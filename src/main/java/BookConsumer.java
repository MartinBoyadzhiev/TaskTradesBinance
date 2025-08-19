import dto.BookUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookConsumer extends Thread {
    private final LinkedBlockingQueue<BookUpdate> buffer;
    //    private final AtomicBoolean hasNewUpdates;
    private final ConcurrentHashMap<String, AtomicBoolean> flagHandler;
    private HashMap<String, LocalOrderBook> books;
    //    private LocalOrderBook orderBook;
    private long lastCalculationTime = 0;
    private static final Logger logger = LoggerFactory.getLogger(BookConsumer.class);

    public BookConsumer(LinkedBlockingQueue<BookUpdate> buffer, ConcurrentHashMap<String, AtomicBoolean> flags) {
        this.buffer = buffer;
        this.flagHandler = flags;
    }

    @Override
    public void run() {
        this.books = new HashMap<>();
        for (String stream : flagHandler.keySet()) {
            books.put(stream, new LocalOrderBook(stream.toUpperCase()));
        }
        waitForDataAndSyncBook();

        while (true) {
            try {
                BookUpdate bookUpdate = buffer.take();
                if (bookUpdate.u() <= books.get(bookUpdate.s().toLowerCase()).getLastUpdateID()) {
                    continue;
                }
                boolean update = books.get(bookUpdate.s().toLowerCase()).update(bookUpdate);
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
            for (String stream : books.keySet()) {
                logger.info("Mid price for {} pair is -> {}", stream.toUpperCase(), books.get(stream).midPrice());
                if (stream.startsWith("btc")) {
                    logger.info("Asks VWAP for {} BTC is {}", 10, books.get(stream).calculateVWAPAsks(10));
                    logger.info("Bids VWAP for {} BTC is {}", 10, books.get(stream).calculateVWAPBids(10));
                }
            }
            lastCalculationTime = System.currentTimeMillis();
        }
    }

    private void waitForDataAndSyncBook() {

        for (String stream : flagHandler.keySet()) {
            flagHandler.computeIfPresent(stream, (k, flag) -> {
                flag.set(false);
                return flag;
            });
        }

        for (String stream : flagHandler.keySet()) {
            while (!flagHandler.get(stream).get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            this.books.get(stream).syncOrderBook();
        }

//        this.hasNewUpdates.set(false);
//
//        while (!hasNewUpdates.get()) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        this.orderBook.syncOrderBook();
    }
}
