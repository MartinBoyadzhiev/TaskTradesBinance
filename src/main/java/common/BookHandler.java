package common;

import common.dto.BookUpdate;

public interface BookHandler {
    void handleUpdateData(BookUpdate update);
    LocalOrderBook getBook();
}
