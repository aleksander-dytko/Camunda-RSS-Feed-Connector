# AI Agent Integration with RSS Feed Connector

This document explains how to use the RSS Feed Connector as a tool in Camunda AI agent subprocesses.

## Overview

The RSS Feed Connector can be integrated as a tool that AI agents can use to fetch and analyze RSS feeds as part of their decision-making process. This enables AI agents to:

- Monitor news feeds and blogs
- Analyze content for relevant information
- Make decisions based on real-time data from RSS sources
- Trigger workflows based on feed content

## AI Agent Tool Configuration

### Tool Definition

When configuring an AI agent in Camunda, the RSS Feed Connector should be registered as an available tool:

```json
{
  "tools": [
    {
      "name": "rss_feed_fetcher",
      "description": "Fetch and parse RSS/Atom feeds from specified URLs",
      "type": "function",
      "function": {
        "name": "fetch_rss_feed",
        "description": "Fetches RSS feed items from a specified URL with optional filtering",
        "parameters": {
          "type": "object",
          "properties": {
            "feedUrl": {
              "type": "string",
              "description": "URL of the RSS/Atom feed to fetch"
            },
            "maxItems": {
              "type": "integer",
              "description": "Maximum number of items to fetch (default: 10)",
              "minimum": 1,
              "maximum": 100
            },
            "authType": {
              "type": "string",
              "enum": ["basic", "bearer"],
              "description": "Type of authentication to use"
            },
            "authToken": {
              "type": "string",
              "description": "Authentication token (for basic: username:password, for bearer: token)"
            },
            "ignoreTls": {
              "type": "boolean",
              "description": "Whether to ignore SSL/TLS certificate validation (default: false)"
            },
            "newerThan": {
              "type": "string",
              "format": "date-time",
              "description": "Only fetch items newer than this timestamp (ISO 8601 format)"
            },
            "guidBlacklist": {
              "type": "array",
              "items": {"type": "string"},
              "description": "List of GUIDs to exclude from results"
            }
          },
          "required": ["feedUrl"]
        }
      }
    }
  ]
}
```

### Example AI Agent Instructions

```text
You are a content monitoring AI agent. You have access to RSS feed fetching capabilities.

Available tools:
- fetch_rss_feed: Fetch RSS feeds from URLs with filtering options

Instructions:
1. When asked to monitor content, use the fetch_rss_feed tool to get the latest items
2. Analyze the content for relevance based on the user's criteria
3. Summarize findings and recommend actions
4. Use filtering options (newerThan, guidBlacklist) to avoid processing duplicate content

Example usage:
- To monitor a news feed: fetch_rss_feed({"feedUrl": "https://feeds.bbci.co.uk/news/rss.xml", "maxItems": 5})
- To get only recent content: fetch_rss_feed({"feedUrl": "https://example.com/feed.xml", "newerThan": "2024-01-01T00:00:00Z"})
```

## BPMN Process Example

The provided BPMN file (`ai-agent-rss-example.bpmn`) shows how to structure a workflow where an AI agent can use the RSS connector as a tool.

### Key Components:

1. **AI Agent Task**: Configured with RSS connector as an available tool
2. **Decision Gateway**: Routes based on AI agent analysis
3. **RSS Feed Task**: Direct RSS fetching when needed
4. **Data Processing**: Handle the RSS data

### Process Flow:

1. AI Agent receives a request to monitor content
2. Agent uses RSS connector tool to fetch relevant feeds
3. Agent analyzes content and makes decisions
4. Process continues based on agent's recommendations

## Input/Output Examples

### AI Agent Input Example

```json
{
  "task": "Monitor tech news for AI-related articles",
  "feeds": [
    "https://feeds.feedburner.com/oreilly/radar",
    "https://techcrunch.com/feed/"
  ],
  "criteria": {
    "keywords": ["artificial intelligence", "machine learning", "AI"],
    "maxItems": 10,
    "newerThan": "2024-01-01T00:00:00Z"
  }
}
```

### AI Agent Tool Call Example

```json
{
  "tool": "fetch_rss_feed",
  "parameters": {
    "feedUrl": "https://feeds.feedburner.com/oreilly/radar",
    "maxItems": 5,
    "newerThan": "2024-01-01T00:00:00Z"
  }
}
```

### Expected Tool Response

```json
{
  "success": true,
  "feedTitle": "O'Reilly Radar",
  "feedDescription": "O'Reilly's take on technology trends",
  "totalItems": 3,
  "items": [
    {
      "title": "The Future of AI in Software Development",
      "description": "Exploring how AI is transforming software development practices...",
      "link": "https://www.oreilly.com/radar/the-future-of-ai-in-software-development/",
      "guid": "https://www.oreilly.com/radar/the-future-of-ai-in-software-development/",
      "pubDate": "2024-01-15T10:30:00Z",
      "author": "John Doe",
      "categories": ["AI", "Software Development"]
    }
  ],
  "fetchedAt": "2024-01-15T12:00:00Z"
}
```

## Best Practices

### 1. Rate Limiting
- Implement delays between RSS feed requests
- Use caching to avoid repeated requests to the same feed
- Respect robots.txt and feed provider guidelines

### 2. Error Handling
- Always check the `success` field in responses
- Handle network timeouts and connection errors
- Implement retry logic with exponential backoff

### 3. Content Filtering
- Use `newerThan` to avoid processing old content
- Maintain `guidBlacklist` to track processed items
- Implement content relevance scoring

### 4. Security
- Store authentication tokens securely
- Validate feed URLs before processing
- Use HTTPS feeds when possible

## Troubleshooting

### Common Issues

1. **Authentication Failures**
   - Verify authType and authToken format
   - Check if the feed requires authentication
   - Test with a simple HTTP client first

2. **SSL/TLS Issues**
   - Set `ignoreTls: true` for testing (not recommended for production)
   - Verify certificate validity
   - Check firewall and proxy settings

3. **Parsing Errors**
   - Verify the URL returns valid RSS/Atom XML
   - Check for encoding issues
   - Test with a known working feed first

### Debug Mode

Enable debug logging to troubleshoot issues:

```yaml
logging:
  level:
    io.camunda.connector.rss: DEBUG
```

## Integration with Camunda Platform

### Connector Registration

Register the connector in your Camunda Platform configuration:

```yaml
camunda:
  connectors:
    rss-feed:
      type: "io.camunda:rss-feed:1"
      class: "io.camunda.connector.rss.RssFeedConnector"
```

### Environment Variables

Set up required environment variables:

```bash
# Optional: Default user agent
RSS_CONNECTOR_USER_AGENT="MyApp/1.0"

# Optional: Default timeout
RSS_CONNECTOR_TIMEOUT_SECONDS="30"
```

This integration enables powerful AI-driven workflows that can monitor and respond to real-time content from RSS feeds.
