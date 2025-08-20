import dto.BookUpdate;
import handles.BookHandle;
import handles.LocalOrderBook;
import handles.QueueHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class BookConsumer extends Thread {
    private final ConcurrentHashMap<String, QueueHandle> queueHandlerMap;
    private HashMap<String, BookHandle> books;
    private static final Logger logger = LoggerFactory.getLogger(BookConsumer.class);
    private final HashMap<String, Long> lastCalculationTimeMap = new HashMap<>();

    public BookConsumer(ConcurrentHashMap<String, QueueHandle> queueHandlerMap) {
        this.queueHandlerMap = queueHandlerMap;
        LocalOrderBook orderBook = new LocalOrderBook("s");
    }

    @Override
    public void run() {
        this.books = new HashMap<>();
        for (String stream : queueHandlerMap.keySet()) {
            books.put(stream, new BookHandle(stream));
        }

        while (true) {
            for (QueueHandle value : queueHandlerMap.values()) {
                if (value.getStreamQueue().peek() != null) {
                    try {
                        BookUpdate updateData = value.getStreamQueue().take();
                        BookHandle bookHandler = books.get(value.getStreamName());
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