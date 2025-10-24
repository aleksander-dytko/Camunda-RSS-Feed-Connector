package io.camunda.connector.rss;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.rss.dto.RssFeedInput;
import io.camunda.connector.rss.dto.RssFeedOutput;
import io.camunda.connector.rss.service.RssFeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RSS Feed Connector for Camunda Platform 8
 * 
 * This connector fetches RSS/Atom feeds from specified URLs and returns structured data
 * that can be used in Camunda workflows or AI agent subprocesses.
 * 
 * Features:
 * - Fetch RSS/Atom feeds from any URL
 * - Configurable maximum items to fetch
 * - Optional authentication (Basic or Bearer)
 * - SSL/TLS configuration
 * - Filtering by timestamp or GUID blacklist
 * - Rate limiting and error handling
 */
@OutboundConnector(
    name = "RSS Feed Connector",
    inputVariables = {"feedUrl", "maxItems", "authType", "authToken", "ignoreTls", "newerThan", "guidBlacklist"},
    type = "io.camunda:rss-feed:1"
)
public class RssFeedConnector implements OutboundConnectorFunction {

    private static final Logger LOG = LoggerFactory.getLogger(RssFeedConnector.class);
    
    private final RssFeedService rssFeedService;
    
    public RssFeedConnector() {
        this.rssFeedService = new RssFeedService();
    }
    
    // Constructor for testing
    public RssFeedConnector(RssFeedService rssFeedService) {
        this.rssFeedService = rssFeedService;
    }

    @Override
    public Object execute(OutboundConnectorContext context) throws Exception {
        LOG.info("Starting RSS Feed Connector execution");
        
        // Parse input from context using bindVariables for Camunda 8.8.1
        RssFeedInput input = context.bindVariables(RssFeedInput.class);
        
        // Validate input
        validateInput(input);
        
        // Execute RSS feed fetching
        RssFeedOutput output = rssFeedService.fetchRssFeed(input);
        
        LOG.info("RSS Feed Connector execution completed successfully. Fetched {} items", 
                output.getItems() != null ? output.getItems().size() : 0);
        
        return output;
    }
    
    private void validateInput(RssFeedInput input) {
        if (input.getFeedUrl() == null || input.getFeedUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("feedUrl is required");
        }
        
        if (input.getMaxItems() != null && input.getMaxItems() < 0) {
            throw new IllegalArgumentException("maxItems must be a positive number");
        }
        
        if (input.getAuthType() != null && !input.getAuthType().equals("basic") && 
            !input.getAuthType().equals("bearer")) {
            throw new IllegalArgumentException("authType must be 'basic' or 'bearer'");
        }
        
        if ((input.getAuthType() != null && !input.getAuthType().isEmpty()) && 
            (input.getAuthToken() == null || input.getAuthToken().trim().isEmpty())) {
            throw new IllegalArgumentException("authToken is required when authType is specified");
        }
    }
}
