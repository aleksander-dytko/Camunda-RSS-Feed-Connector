package io.camunda.connector.rss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing an RSS enclosure (media attachment)
 */
public class RssEnclosure {
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("length")
    private Long length;
    
    // Constructors
    public RssEnclosure() {}
    
    public RssEnclosure(String url, String type, Long length) {
        this.url = url;
        this.type = type;
        this.length = length;
    }
    
    // Getters and Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getLength() {
        return length;
    }
    
    public void setLength(Long length) {
        this.length = length;
    }
    
    @Override
    public String toString() {
        return "RssEnclosure{" +
                "url='" + url + '\'' +
                ", type='" + type + '\'' +
                ", length=" + length +
                '}';
    }
}
