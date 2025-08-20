import dto.BookUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class BookConsumer extends Thread {
    private final ConcurrentHashMap<String, QueueHandler> queueHandlerMap;
    private HashMap<String, BookHandler> books;
    private static final Logger logger = LoggerFactory.getLogger(BookConsumer.class);
    private final HashMap<String, Long> lastCalculationTimeMap = new HashMap<>();

    public BookConsumer(ConcurrentHashMap<String, QueueHandler> queueHandlerMap) {
        this.queueHandlerMap = queueHandlerMap;
    }

    @Override
    public void run() {
        this.books = new HashMap<>();
        for (String stream : queueHandlerMap.keySet()) {
            books.put(stream, new BookHandler(stream));
        }

        while (true) {
            for (QueueHandler value : queueHandlerMap.values()) {
                if (value.getStreamQueue().peek() != null) {
                    try {
                        BookUpdate updateData = value.getStreamQueue().take();
                        BookHandler bookHandler = books.get(value.getStreamName());
                        bookHandler.update(updateData);
                        doCalculations(bookHandler.getLocalBook());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void doCalculations(LocalOrderBook orderBook) {
        Long lastCalculationTime = lastCalculationTimeMap.computeIfAbsent(orderBook.getSymbol(), k -> System.currentTimeMillis());
        if (System.currentTimeMillis() - lastCalculationTime > 1000) {
            logger.info("Mid price for {}: {}", orderBook.getSymbol().toUpperCase(), orderBook.midPrice());
            if (orderBook.getSymbol().startsWith("btc")) {
                logger.info("Asks VWAP for 10 BTC is {}", orderBook.calculateVWAPAsks(10));
                logger.info("Bids VWAP for 10 BTC is {}", orderBook.calculateVWAPBids(10));
            }
            this.lastCalculationTimeMap.put(orderBook.getSymbol(), System.currentTimeMillis());
        }
    }
}