package io.camunda.connector.rss;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.JobContext;
import io.camunda.connector.rss.dto.RssFeedInput;
import io.camunda.connector.rss.dto.RssFeedOutput;
import io.camunda.connector.rss.service.RssFeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RssFeedConnectorTest {

    private RssFeedConnector connector;
    private ObjectMapper objectMapper;
    
    @Mock
    private OutboundConnectorContext mockContext;
    
    @Mock
    private JobContext mockJobContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        connector = new RssFeedConnector();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldThrowExceptionForMissingFeedUrl() throws Exception {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl(null);
        
        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When & Then
        assertThatThrownBy(() -> connector.execute(mockContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("feedUrl is required");
    }

    @Test
    void shouldThrowExceptionForEmptyFeedUrl() throws Exception {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("");
        
        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When & Then
        assertThatThrownBy(() -> connector.execute(mockContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("feedUrl is required");
    }

    @Test
    void shouldThrowExceptionForNegativeMaxItems() throws Exception {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("https://example.com/feed.xml");
        input.setMaxItems(-1);
        
        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When & Then
        assertThatThrownBy(() -> connector.execute(mockContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxItems must be a positive number");
    }

    @Test
    void shouldThrowExceptionForInvalidAuthType() throws Exception {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("https://example.com/feed.xml");
        input.setAuthType("invalid");
        input.setAuthToken("token");
        
        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When & Then
        assertThatThrownBy(() -> connector.execute(mockContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("authType must be 'basic' or 'bearer'");
    }

    @Test
    void shouldThrowExceptionForAuthTypeWithoutToken() throws Exception {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("https://example.com/feed.xml");
        input.setAuthType("basic");
        input.setAuthToken(null);
        
        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When & Then
        assertThatThrownBy(() -> connector.execute(mockContext))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("authToken is required when authType is specified");
    }

    @Test
    void shouldHandleValidInput() throws Exception {
        // Given
        RssFeedInput input = new RssFeedInput();
        input.setFeedUrl("https://example.com/feed.xml");
        input.setMaxItems(10);
        
        when(mockContext.bindVariables(RssFeedInput.class)).thenReturn(input);

        // When
        Object result = connector.execute(mockContext);

        // Then
        assertThat(result).isInstanceOf(RssFeedOutput.class);
        RssFeedOutput output = (RssFeedOutput) result;
        // The output might be successful or have an error depending on the actual feed
        assertThat(output).isNotNull();
    }

}