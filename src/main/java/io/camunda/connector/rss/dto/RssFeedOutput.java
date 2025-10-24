package io.camunda.connector.rss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * Output DTO for RSS Feed Connector
 */
public class RssFeedOutput {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("feedTitle")
    private String feedTitle;
    
    @JsonProperty("feedDescription")
    private String feedDescription;
    
    @JsonProperty("feedLink")
    private String feedLink;
    
    @JsonProperty("feedLanguage")
    private String feedLanguage;
    
    @JsonProperty("items")
    private List<RssItem> items;
    
    @JsonProperty("totalItems")
    private int totalItems;
    
    @JsonProperty("fetchedAt")
    private String fetchedAt;
    
    @JsonProperty("error")
    private String error;
    
    // Constructors
    public RssFeedOutput() {
        this.success = true;
        this.fetchedAt = Instant.now().toString();
    }
    
    public RssFeedOutput(boolean success, String error) {
        this.success = success;
        this.error = error;
        this.fetchedAt = Instant.now().toString();
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getFeedTitle() {
        return feedTitle;
    }
    
    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }
    
    public String getFeedDescription() {
        return feedDescription;
    }
    
    public void setFeedDescription(String feedDescription) {
        this.feedDescription = feedDescription;
    }
    
    public String getFeedLink() {
        return feedLink;
    }
    
    public void setFeedLink(String feedLink) {
        this.feedLink = feedLink;
    }
    
    public String getFeedLanguage() {
        return feedLanguage;
    }
    
    public void setFeedLanguage(String feedLanguage) {
        this.feedLanguage = feedLanguage;
    }
    
    public List<RssItem> getItems() {
        return items;
    }
    
    public void setItems(List<RssItem> items) {
        this.items = items;
        this.totalItems = items != null ? items.size() : 0;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    public String getFetchedAt() {
        return fetchedAt;
    }
    
    public void setFetchedAt(String fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "RssFeedOutput{" +
                "success=" + success +
                ", feedTitle='" + feedTitle + '\'' +
                ", totalItems=" + totalItems +
                ", fetchedAt='" + fetchedAt + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
