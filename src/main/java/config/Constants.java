package config;

public class Constants {
    public static final String REST_ENDPOINT_TEMPLATE = System.getenv("REST_ENDPOINT_TEMPLATE");
    public static final String BINANCE_PAIR = System.getenv("BINANCE_PAIR");
    public static final int SNAPSHOT_LEVELS = Integer.parseInt(System.getenv("SNAPSHOT_LEVELS"));
    public static final String EXCHANGES =  System.getenv("EXCHANGES");
}
