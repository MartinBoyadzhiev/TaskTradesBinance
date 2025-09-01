package config;

public class Constants {
    public static String REST_ENDPOINT_TEMPLATE = System.getenv("REST_ENDPOINT_TEMPLATE");
    public static String BINANCE_PAIR = System.getenv("BINANCE_PAIR");
    public static int SNAPSHOT_LEVELS = Integer.parseInt(System.getenv("SNAPSHOT_LEVELS"));
}
