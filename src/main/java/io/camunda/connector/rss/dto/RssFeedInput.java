package io.camunda.connector.rss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Input DTO for RSS Feed Connector
 */
public class RssFeedInput {
    
    @JsonProperty("feedUrl")
    private String feedUrl;
    
    @JsonProperty("maxItems")
    private Integer maxItems = 10;
    
    @JsonProperty("authType")
    private String authType; // "basic" or "bearer"
    
    @JsonProperty("authToken")
    private String authToken;
    
    @JsonProperty("ignoreTls")
    private Boolean ignoreTls = false;
    
    @JsonProperty("newerThan")
    private String newerThan; // ISO 8601 timestamp
    
    @JsonProperty("guidBlacklist")
    private List<String> guidBlacklist;
    
    @JsonProperty("userAgent")
    private String userAgent = "Camunda-RSS-Connector/1.0";
    
    @JsonProperty("timeoutSeconds")
    private Integer timeoutSeconds = 30;
    
    // Constructors
    public RssFeedInput() {}
    
    public RssFeedInput(String feedUrl) {
        this.feedUrl = feedUrl;
    }
    
    // Getters and Setters
    public String getFeedUrl() {
        return feedUrl;
    }
    
    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }
    
    public Integer getMaxItems() {
        return maxItems;
    }
    
    public void setMaxItems(Integer maxItems) {
        this.maxItems = maxItems;
    }
    
    public String getAuthType() {
        return authType;
    }
    
    public void setAuthType(String authType) {
        this.authType = authType;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public Boolean getIgnoreTls() {
        return ignoreTls;
    }
    
    public void setIgnoreTls(Boolean ignoreTls) {
        this.ignoreTls = ignoreTls;
    }
    
    public String getNewerThan() {
        return newerThan;
    }
    
    public void setNewerThan(String newerThan) {
        this.newerThan = newerThan;
    }
    
    public List<String> getGuidBlacklist() {
        return guidBlacklist;
    }
    
    public void setGuidBlacklist(List<String> guidBlacklist) {
        this.guidBlacklist = guidBlacklist;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }
    
    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
    
    @Override
    public String toString() {
        return "RssFeedInput{" +
                "feedUrl='" + feedUrl + '\'' +
                ", maxItems=" + maxItems +
                ", authType='" + authType + '\'' +
                ", ignoreTls=" + ignoreTls +
                ", newerThan='" + newerThan + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                '}';
    }
    
    /**
     * Safe toString method that masks sensitive information for logging
     */
    public String toSafeString() {
        return "RssFeedInput{" +
                "feedUrl='" + feedUrl + '\'' +
                ", maxItems=" + maxItems +
                ", authType='" + authType + '\'' +
                ", authToken='" + (authToken != null ? "***MASKED***" : "null") + '\'' +
                ", ignoreTls=" + ignoreTls +
                ", newerThan='" + newerThan + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                '}';
    }
}
