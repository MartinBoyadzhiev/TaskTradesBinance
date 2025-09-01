package common;

import common.dto.BookUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class DataConsumer extends Thread {
    private final HashMap<String, Map<String, QueueHandle>> queueHandlersPerExchangeMap;
    private final HashMap<String, Map<String, BookHandler>> bookHandlersPerExchangeMap;
    private final Logger logger = LoggerFactory.getLogger(DataConsumer.class);
    private final HashMap<String, Long> lastCalculationTimeMap = new HashMap<>();

    public DataConsumer(HashMap<String, Map<String, QueueHandle>> queueHandlersPerExchangeMap, HashMap<String, Map<String, BookHandler>> bookHandlersPerExchangeMap) {
        this.queueHandlersPerExchangeMap = queueHandlersPerExchangeMap;
        this.bookHandlersPerExchangeMap = bookHandlersPerExchangeMap;
    }

    @Override
    public void run() {
        while (true) {
            for (String exchange : queueHandlersPerExchangeMap.keySet()) {
                Map<String, QueueHandle> queueHandleMap = queueHandlersPerExchangeMap.get(exchange);
                for (Map.Entry<String, QueueHandle> queueHandleEntry : queueHandleMap.entrySet()) {
                    LinkedBlockingQueue<BookUpdate> streamQueue = queueHandleEntry.getValue().getStreamQueue();
                    if (streamQueue.peek() != null) {
                        try {
                            BookUpdate updateData = streamQueue.take();
                            BookHandler bookHandler = bookHandlersPerExchangeMap.get(exchange).get(queueHandleEntry.getKey());
                            bookHandler.handleUpdateData(updateData);
                            doCalculations(bookHandler.getBook());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
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