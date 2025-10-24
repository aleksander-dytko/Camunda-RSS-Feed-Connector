package io.camunda.connector.rss.service;

import io.camunda.connector.rss.dto.RssFeedInput;
import io.camunda.connector.rss.dto.RssFeedOutput;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RssFeedServiceTest {

    private RssFeedService rssFeedService;
    private OkHttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new OkHttpClient();
        rssFeedService = new RssFeedService(httpClient);
    }

    @Test
    void shouldCreateHttpClientWithSslConfig() {
        // Given
        boolean ignoreTls = false;

        // When
        OkHttpClient client = RssFeedService.createHttpClientWithSslConfig(ignoreTls);

        // Then
        assertThat(client).isNotNull();
        assertThat(client.connectTimeoutMillis()).isGreaterThan(0);
        assertThat(client.readTimeoutMillis()).isGreaterThan(0);
    }

    @Test
    void shouldCreateHttpClientWithIgnoreTls() {
        // Given
        boolean ignoreTls = true;

        // When
        OkHttpClient client = RssFeedService.createHttpClientWithSslConfig(ignoreTls);

        // Then
        assertThat(client).isNotNull();
        assertThat(client.connectTimeoutMillis()).isGreaterThan(0);
        assertThat(client.readTimeoutMillis()).isGreaterThan(0);
    }

    @Test
    void shouldHandleInvalidUrl() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("invalid-url");

        // When
        RssFeedOutput output = rssFeedService.fetchRssFeed(input);

        // Then
        assertThat(output.isSuccess()).isFalse();
        assertThat(output.getError()).isNotEmpty();
    }

    @Test
    void shouldHandleNullInput() {
        // Given
        RssFeedInput input = null;

        // When & Then
        assertThatThrownBy(() -> rssFeedService.fetchRssFeed(input))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldHandleEmptyUrl() {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("");

        // When
        RssFeedOutput output = rssFeedService.fetchRssFeed(input);

        // Then
        assertThat(output.isSuccess()).isFalse();
        assertThat(output.getError()).isNotEmpty();
    }
}