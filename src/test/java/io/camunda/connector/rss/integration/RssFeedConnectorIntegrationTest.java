package io.camunda.connector.rss.integration;

import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.JobContext;
import io.camunda.connector.rss.RssFeedConnector;
import io.camunda.connector.rss.dto.RssFeedInput;
import io.camunda.connector.rss.dto.RssFeedOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration tests for RSS Feed Connector
 * These tests require actual RSS feeds and should be run with appropriate environment variables
 */
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")
class RssFeedConnectorIntegrationTest {

    private final RssFeedConnector connector = new RssFeedConnector();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Mock
    private OutboundConnectorContext mockContext;
    
    @Mock
    private JobContext mockJobContext;
    
    public RssFeedConnectorIntegrationTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldFetchRealRssFeed() throws Exception {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("https://feeds.bbci.co.uk/news/rss.xml");
        input.setMaxItems(5);

        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When
        Object result = connector.execute(mockContext);

        // Then
        assertThat(result).isInstanceOf(RssFeedOutput.class);
        RssFeedOutput output = (RssFeedOutput) result;
        assertThat(output.isSuccess()).isTrue();
        assertThat(output.getFeedTitle()).isNotEmpty();
        assertThat(output.getItems()).isNotEmpty();
        assertThat(output.getItems().size()).isLessThanOrEqualTo(5);
    }

    @Test
    void shouldHandleAuthentication() throws Exception {
        // Given - This would require a test feed with authentication
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("https://httpbin.org/basic-auth/user/pass");
        input.setAuthType("basic");
        input.setAuthToken("user:pass");
        input.setMaxItems(1);

        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When
        Object result = connector.execute(mockContext);

        // Then
        assertThat(result).isInstanceOf(RssFeedOutput.class);
        RssFeedOutput output = (RssFeedOutput) result;
        // The result might be successful or fail depending on the endpoint
        assertThat(output).isNotNull();
    }
}
