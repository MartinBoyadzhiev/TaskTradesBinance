import common.DataConsumer;
import config.Constants;
import handles.BinanceBookHandler;
import common.BookHandler;
import common.QueueHandle;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        List<String> subscriptionPairs = Arrays.stream(Constants.BINANCE_PAIR.split(",")).toList();
        List<String> exchanges = Arrays.stream(Constants.EXCHANGES.split(",")).toList();

        HashMap<String, Map<String, QueueHandle>> queueHandleMap = initiateQueueMap(exchanges, subscriptionPairs);
        BinanceWebSocketConnector ws = new BinanceWebSocketConnector(queueHandleMap);
        ws.connect();

        HashMap<String, Map<String, BookHandler>> bookHandlerMap = initiateBookHandlerMap(exchanges, subscriptionPairs);

        DataConsumer dataConsumer = new DataConsumer(queueHandleMap, bookHandlerMap);
        dataConsumer.start();
    }

    private static HashMap<String, Map<String, BookHandler>> initiateBookHandlerMap(List<String> exchanges, List<String> subscriptionPairs) {
        HashMap<String, Map<String, BookHandler>> bookHandlersPerExchangeMap = new HashMap<>();
        for (String exchange : exchanges) {
            bookHandlersPerExchangeMap.put(exchange, new HashMap<>());
            Map<String, BookHandler> bookHandlerMap = bookHandlersPerExchangeMap.get(exchange);
            for (String pair : subscriptionPairs) {
                if (exchange.equals("binance")) {
                    bookHandlerMap.put(pair, new BinanceBookHandler(pair));
                } else if (exchange.equals("kraken")) {
//                    bookHandlerMap.put(pair, new KrakenBookHandler());
                }
            }
        }
        return bookHandlersPerExchangeMap;
    }

    private static HashMap<String, Map<String, QueueHandle>> initiateQueueMap(List<String> exchanges, List<String> subscriptionPairs) {
        HashMap<String, Map<String, QueueHandle>> queueHandlePerExchangeMap = new HashMap<>();
        for (String exchange : exchanges) {
            queueHandlePerExchangeMap.put(exchange, new HashMap<>());
            Map<String, QueueHandle> queueHandleMap = queueHandlePerExchangeMap.get(exchange);
            for (String pair : subscriptionPairs) {
                queueHandleMap.put(pair, new QueueHandle(pair, new LinkedBlockingQueue<>()));
            }
        }
        return queueHandlePerExchangeMap;
    }
}