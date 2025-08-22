package enums;

public enum EnvVar {
    REST_ENDPOINT_TEMPLATE("REST_ENDPOINT_TEMPLATE"),
    BINANCE_PAIR("BINANCE_PAIR"),
    SNAPSHOT_LEVELS("SNAPSHOT_LEVELS");

    private final String key;

    EnvVar(String key) {
        this.key = key;
    }

    public String get() {
        return System.getenv(key);
    }

    public int getInt() {
        return Integer.parseInt(System.getenv(key));
    }
}
