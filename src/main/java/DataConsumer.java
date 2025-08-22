import dto.BookUpdate;
import handles.BookHandle;
import handles.LocalOrderBook;
import handles.QueueHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class DataConsumer extends Thread {

    private final ConcurrentHashMap<String, QueueHandle> queueHandlerMap;
    private final HashMap<String, BookHandle> books = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(DataConsumer.class);
    private final HashMap<String, Long> lastCalculationTimeMap = new HashMap<>();

    public DataConsumer(ConcurrentHashMap<String, QueueHandle> queueHandlerMap) {
        this.queueHandlerMap = queueHandlerMap;
    }

    @Override
    public void run() {
        for (String pairName : queueHandlerMap.keySet()) {
            this.books.put(pairName, new BookHandle(pairName));
        }

        while (true) {
            for (QueueHandle queueHandle : queueHandlerMap.values()) {
                if (queueHandle.getStreamQueue().peek() != null) {
                    try {
                        BookUpdate updateData = queueHandle.getStreamQueue().take();
                        BookHandle bookHandler = this.books.get(queueHandle.getStreamName());
                        bookHandler.handleUpdateData(updateData);
                        doCalculations(bookHandler.getBook());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void doCalculations(LocalOrderBook orderBook) {
        Long lastCalculationTime = lastCalculationTimeMap.computeIfAbsent(orderBook.getPairName(), k -> System.currentTimeMillis());
        if (System.currentTimeMillis() - lastCalculationTime > 1000) {
            logger.info("Mid price for {}: {}", orderBook.getPairName().toUpperCase(), orderBook.getMidPrice());
            if (orderBook.getPairName().startsWith("btc")) {
                logger.info("Asks VWAP for 10 BTC is {}", orderBook.calculateVWAPAsks(10));
                logger.info("Bids VWAP for 10 BTC is {}", orderBook.calculateVWAPBids(10));
            }
            this.lastCalculationTimeMap.put(orderBook.getPairName(), System.currentTimeMillis());
        }
    }
}