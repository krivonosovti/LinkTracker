package backend.academy.scrapper.dto;

import java.time.Instant;

public class ExternalResourceUpdate {
    private String url;
    private String description;
    private Instant lastUpdated;

    public ExternalResourceUpdate() {
    }

    public ExternalResourceUpdate(String url, String description, Instant lastUpdated) {
        this.url = url;
        this.description = description;
        this.lastUpdated = lastUpdated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    /**
     * Проверяет, обновился ли ресурс по сравнению с ранее сохранённым временем.
     *
     * @param previous предыдущая дата обновления (может быть null)
     * @return true, если ресурс обновлён, иначе false
     */
    public boolean isUpdated(Instant previous) {
        return previous == null || (lastUpdated != null && lastUpdated.isAfter(previous));
    }
}
