package backend.academy.scrapper.dto;

import backend.academy.scrapper.model.StackExchangeQuestionItem;
import java.util.List;

public class StackExchangeQuestionResponse {
    private List<StackExchangeQuestionItem> items;

    public List<StackExchangeQuestionItem> getItems() {
        return items;
    }

    public void setItems(List<StackExchangeQuestionItem> items) {
        this.items = items;
    }
}
