package io.camunda.connector.rss.security;

import io.camunda.connector.rss.dto.RssFeedInput;
import io.camunda.connector.rss.service.RssFeedService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Security tests for RSS Feed Connector
 */
class SecurityTest {

    private RssFeedService rssFeedService;

    @BeforeEach
    void setUp() {
        rssFeedService = new RssFeedService();
    }

    @Test
    void shouldRejectPrivateIpAddresses() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("http://192.168.1.1/feed.xml");

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access to private/internal networks is not allowed");
    }

    @Test
    void shouldRejectLocalhostAddresses() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("http://localhost/feed.xml");

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Access to private/internal networks is not allowed");
    }

    @Test
    void shouldRejectFileProtocol() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("file:///etc/passwd");

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only HTTP and HTTPS protocols are allowed");
    }

    @Test
    void shouldRejectFtpProtocol() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("ftp://example.com/feed.xml");

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Only HTTP and HTTPS protocols are allowed");
    }

    @Test
    void shouldRejectInvalidUrl() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("not-a-valid-url");

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid URL format");
    }

    @Test
    void shouldRejectEmptyUrl() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("");

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid URL format");
    }

    @Test
    void shouldRejectNullUrl() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl(null);

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid URL format");
    }

    @Test
    void shouldAcceptValidHttpsUrl() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("https://feeds.bbci.co.uk/news/rss.xml");

        // When & Then - Should not throw security exception
        // Note: This might fail due to network issues, but not security issues
        try {
            rssFeedService.fetchRssFeed(input);
        } catch (Exception e) {
            // Should not be a security exception
            assertThatThrownBy(() -> { throw e; })
                    .isNotInstanceOf(SecurityException.class);
        }
    }

    @Test
    void shouldAcceptValidHttpUrl() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("http://example.com/feed.xml");

        // When & Then - Should not throw security exception
        // Note: This might fail due to network issues, but not security issues
        try {
            rssFeedService.fetchRssFeed(input);
        } catch (Exception e) {
            // Should not be a security exception
            assertThatThrownBy(() -> { throw e; })
                    .isNotInstanceOf(SecurityException.class);
        }
    }
}
