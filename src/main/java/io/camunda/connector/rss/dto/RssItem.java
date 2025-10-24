package io.camunda.connector.rss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

/**
 * DTO representing a single RSS item
 */
public class RssItem {
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("link")
    private String link;
    
    @JsonProperty("guid")
    private String guid;
    
    @JsonProperty("pubDate")
    private String pubDate;
    
    @JsonProperty("author")
    private String author;
    
    @JsonProperty("categories")
    private List<String> categories;
    
    @JsonProperty("enclosures")
    private List<RssEnclosure> enclosures;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("comments")
    private String comments;
    
    // Constructors
    public RssItem() {}
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public String getGuid() {
        return guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }
    
    public String getPubDate() {
        return pubDate;
    }
    
    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public List<String> getCategories() {
        return categories;
    }
    
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    
    public List<RssEnclosure> getEnclosures() {
        return enclosures;
    }
    
    public void setEnclosures(List<RssEnclosure> enclosures) {
        this.enclosures = enclosures;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    @Override
    public String toString() {
        return "RssItem{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", guid='" + guid + '\'' +
                ", pubDate='" + pubDate + '\'' +
                '}';
    }
}
