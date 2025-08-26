package dto;

public abstract class BookUpdate {
    private String pairName;

    public BookUpdate() {
    }

    public String getPairName() {
        return pairName;
    }

    public void setPairName(String pairName) {
        this.pairName = pairName;
    }
}
