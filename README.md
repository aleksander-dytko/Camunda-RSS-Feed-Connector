# RSS Feed Connector for Camunda Platform 8

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Camunda Connector SDK](https://img.shields.io/badge/Camunda-Connector%20SDK-blue)](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/)
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/projects/jdk/17/)

A powerful Camunda Connector for fetching and parsing RSS/Atom feeds. Perfect for building automated workflows that monitor news feeds, blogs, podcasts, or any RSS-enabled content source.

## Features

- 🔗 **Universal RSS/Atom Support** - Works with any RSS 2.0 or Atom feed
- 🔐 **Authentication** - Basic and Bearer token authentication support
- 🛡️ **SSL/TLS Configuration** - Flexible SSL certificate handling
- 📅 **Smart Filtering** - Filter by timestamp, GUID blacklist, and item limits
- 🤖 **AI Agent Ready** - Perfect for use in Camunda AI agent subprocesses
- ⚡ **High Performance** - Efficient HTTP client with connection pooling
- 🧪 **Well Tested** - Comprehensive unit and integration tests

## Quick Start

### Installation

1. **Download the connector JAR** from the [Camunda Marketplace](https://marketplace.camunda.io/)
2. **Deploy to your Camunda Platform 8** environment
3. **Configure in your BPMN processes**

### Basic Usage

```xml
<bpmn:serviceTask id="fetchRssTask" name="Fetch RSS Feed" 
                 camunda:type="external" camunda:topic="rss-feed">
  <bpmn:extensionElements>
    <zeebe:taskDefinition type="rss-feed" />
    <zeebe:taskHeaders>
      <zeebe:header key="connector" value="io.camunda:rss-feed:1" />
    </zeebe:taskHeaders>
  </bpmn:extensionElements>
</bpmn:serviceTask>
```

### Input Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `feedUrl` | String | ✅ | URL of the RSS/Atom feed to fetch |
| `maxItems` | Integer | ❌ | Maximum number of items to fetch (default: 10) |
| `authType` | String | ❌ | Authentication type: "basic" or "bearer" |
| `authToken` | String | ❌ | Authentication token (secret) |
| `ignoreTls` | Boolean | ❌ | Ignore SSL/TLS certificate validation (default: false) |
| `newerThan` | String | ❌ | Only fetch items newer than this timestamp (ISO 8601) |
| `guidBlacklist` | Array | ❌ | List of GUIDs to exclude from results |
| `userAgent` | String | ❌ | Custom User-Agent string |
| `timeoutSeconds` | Integer | ❌ | Request timeout in seconds (default: 30) |

### Output Structure

```json
{
  "success": true,
  "feedTitle": "Example News Feed",
  "feedDescription": "Latest news and updates",
  "feedLink": "https://example.com",
  "feedLanguage": "en-us",
  "totalItems": 5,
  "items": [
    {
      "title": "Breaking News Article",
      "description": "Article description...",
      "link": "https://example.com/article1",
      "guid": "https://example.com/article1",
      "pubDate": "2024-01-15T10:30:00Z",
      "author": "John Doe",
      "categories": ["News", "Breaking"],
      "enclosures": [
        {
          "url": "https://example.com/image.jpg",
          "type": "image/jpeg",
          "length": 1024000
        }
      ]
    }
  ],
  "fetchedAt": "2024-01-15T12:00:00Z"
}
```

## Use Cases

### 1. News Monitoring
Monitor news feeds for specific topics and trigger alerts or workflows.

### 2. Content Aggregation
Collect content from multiple RSS sources for analysis or display.

### 3. AI Agent Integration
Use as a tool in Camunda AI agent subprocesses for intelligent content analysis.

### 4. Automated Reporting
Generate reports based on RSS feed content with filtering and analysis.

## Examples

### Basic RSS Fetching

```json
{
  "feedUrl": "https://feeds.bbci.co.uk/news/rss.xml",
  "maxItems": 5
}
```

### With Authentication

```json
{
  "feedUrl": "https://private-feed.example.com/rss.xml",
  "authType": "bearer",
  "authToken": "your-api-token",
  "maxItems": 10
}
```

### With Filtering

```json
{
  "feedUrl": "https://techcrunch.com/feed/",
  "maxItems": 20,
  "newerThan": "2024-01-01T00:00:00Z",
  "guidBlacklist": ["https://techcrunch.com/old-article"]
}
```

## AI Agent Integration

The RSS Feed Connector is perfect for use with Camunda AI agents. See the [AI Agent Integration Guide](docs/ai-agent-integration.md) for detailed examples.

### Example AI Agent Tool Call

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

## Security Considerations

- 🔒 **Secrets Management**: Use Camunda's secret management for authentication tokens
- 🛡️ **SSL/TLS**: Prefer HTTPS feeds and validate certificates in production
- 🚫 **Rate Limiting**: Implement appropriate delays between requests
- 🔍 **Input Validation**: Always validate feed URLs before processing
- 📝 **Logging**: Monitor and log all RSS feed requests for security auditing

## Development

### Prerequisites

- Java 17+
- Maven 3.6+
- Camunda Platform 8.8.0+

### Building

```bash
# Clone the repository
git clone https://github.com/camunda/rss-feed-connector.git
cd rss-feed-connector

# Build the connector
mvn clean package

# Run tests
mvn test

# Run integration tests (requires RUN_INTEGRATION_TESTS=true)
RUN_INTEGRATION_TESTS=true mvn test
```

### Testing

```bash
# Unit tests only
mvn test

# Integration tests (requires internet connection)
RUN_INTEGRATION_TESTS=true mvn test

# All tests with coverage
mvn clean test jacoco:report
```

## Configuration

### Environment Variables

```bash
# Optional: Default user agent
RSS_CONNECTOR_USER_AGENT="MyApp/1.0"

# Optional: Default timeout
RSS_CONNECTOR_TIMEOUT_SECONDS="30"
```

### Camunda Platform Configuration

```yaml
camunda:
  connectors:
    rss-feed:
      type: "io.camunda:rss-feed:1"
      class: "io.camunda.connector.rss.RssFeedConnector"
```

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

Enable debug logging:

```yaml
logging:
  level:
    io.camunda.connector.rss: DEBUG
```

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

- 📖 **Documentation**: [Camunda Connector SDK](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/)
- 🐛 **Issues**: [GitHub Issues](https://github.com/camunda/rss-feed-connector/issues)
- 💬 **Discussions**: [GitHub Discussions](https://github.com/camunda/rss-feed-connector/discussions)
- 📧 **Contact**: [Camunda Support](https://camunda.com/support/)

## Changelog

### v1.0.0
- Initial release
- RSS/Atom feed parsing
- Authentication support
- SSL/TLS configuration
- Filtering capabilities
- AI agent integration
- Comprehensive testing

---

**Made with ❤️ by the Camunda Community**