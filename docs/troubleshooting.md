# Troubleshooting Guide for RSS Feed Connector

## Common Issues and Solutions

### 1. Connection Issues

#### Problem: "Connection timeout" or "Connection refused"

**Symptoms:**
- Connector fails with timeout errors
- HTTP 5xx errors from the server
- Network connectivity issues

**Solutions:**
```yaml
# Increase timeout settings
timeoutSeconds: 60
maxRetries: 3
retryDelaySeconds: 5
```

**Debug Steps:**
1. Test the RSS URL in a browser
2. Check network connectivity
3. Verify firewall settings
4. Test with a known working feed

#### Problem: "SSL handshake failed"

**Symptoms:**
- SSL/TLS certificate errors
- "Certificate verify failed" messages
- HTTPS connection issues

**Solutions:**
```json
{
  "feedUrl": "https://example.com/feed.xml",
  "ignoreTls": true
}
```

**Note:** Only use `ignoreTls: true` in testing environments, not production.

**Debug Steps:**
1. Check certificate validity: `openssl s_client -connect example.com:443`
2. Verify certificate chain
3. Check for expired certificates
4. Test with HTTP instead of HTTPS

### 2. Authentication Issues

#### Problem: "Authentication failed" or "401 Unauthorized"

**Symptoms:**
- HTTP 401 errors
- Authentication token rejected
- Access denied messages

**Solutions:**

**Basic Authentication:**
```json
{
  "feedUrl": "https://private-feed.example.com/rss.xml",
  "authType": "basic",
  "authToken": "username:password"
}
```

**Bearer Token Authentication:**
```json
{
  "feedUrl": "https://api.example.com/feed",
  "authType": "bearer",
  "authToken": "your-api-token"
}
```

**Debug Steps:**
1. Verify credentials with a simple HTTP client
2. Check token expiration
3. Test with curl: `curl -u username:password https://feed-url`
4. Verify authentication method (basic vs bearer)

#### Problem: "403 Forbidden" or "Access denied"

**Symptoms:**
- HTTP 403 errors
- "Access denied" messages
- Authentication succeeds but access is denied

**Solutions:**
1. Check user permissions for the RSS feed
2. Verify API key permissions
3. Contact feed provider for access
4. Check for IP restrictions

### 3. Parsing Issues

#### Problem: "Failed to parse RSS feed" or "Invalid XML"

**Symptoms:**
- XML parsing errors
- "Invalid feed format" messages
- Empty or malformed RSS content

**Solutions:**

**Check Feed Format:**
```bash
# Validate RSS feed
curl -s "https://example.com/feed.xml" | xmllint --format -
```

**Common Issues:**
1. **Invalid XML**: Feed contains malformed XML
2. **Encoding Issues**: Character encoding problems
3. **Empty Feed**: Feed is empty or returns no content
4. **Wrong Content Type**: Server returns wrong MIME type

**Debug Steps:**
1. Test feed URL in browser
2. Check response headers: `curl -I https://example.com/feed.xml`
3. Validate XML structure
4. Check for encoding issues

#### Problem: "No items found" or empty results

**Symptoms:**
- Connector succeeds but returns no items
- Empty `items` array in response
- Filtering too restrictive

**Solutions:**

**Check Filtering:**
```json
{
  "feedUrl": "https://example.com/feed.xml",
  "maxItems": 10,
  "newerThan": null,
  "guidBlacklist": []
}
```

**Debug Steps:**
1. Remove all filters to test
2. Check feed content manually
3. Verify timestamp formats
4. Test with different `maxItems` values

### 4. Performance Issues

#### Problem: "Request timeout" or slow responses

**Symptoms:**
- Long response times
- Timeout errors
- Slow feed processing

**Solutions:**

**Optimize Settings:**
```json
{
  "feedUrl": "https://example.com/feed.xml",
  "maxItems": 5,
  "timeoutSeconds": 30
}
```

**Debug Steps:**
1. Test with smaller `maxItems`
2. Check feed server performance
3. Monitor network latency
4. Implement caching if appropriate

#### Problem: "Out of memory" or high memory usage

**Symptoms:**
- Memory errors
- High memory consumption
- OutOfMemoryError exceptions

**Solutions:**
1. Reduce `maxItems` parameter
2. Implement streaming parsing for large feeds
3. Increase JVM heap size
4. Use feed pagination if available

### 5. Filtering Issues

#### Problem: "No items match filter criteria"

**Symptoms:**
- All items filtered out
- Empty results despite feed having content
- Incorrect timestamp filtering

**Solutions:**

**Debug Filtering:**
```json
{
  "feedUrl": "https://example.com/feed.xml",
  "maxItems": 10,
  "newerThan": "2023-01-01T00:00:00Z",
  "guidBlacklist": []
}
```

**Common Issues:**
1. **Timestamp Format**: Incorrect ISO 8601 format
2. **Timezone Issues**: Timestamp timezone problems
3. **GUID Blacklist**: Overly restrictive blacklist
4. **Date Parsing**: RSS date format issues

**Debug Steps:**
1. Test without filters first
2. Check timestamp formats in feed
3. Verify GUID values
4. Test with different date ranges

## Debug Mode

### Enable Debug Logging

```yaml
logging:
  level:
    io.camunda.connector.rss: DEBUG
    okhttp3: DEBUG
    com.rometools: DEBUG
```

### Debug Information

The connector logs the following debug information:
- HTTP request details
- Response status and headers
- RSS parsing steps
- Filtering results
- Error details

### Example Debug Output

```
DEBUG [RssFeedConnector] Starting RSS feed fetch: https://example.com/feed.xml
DEBUG [RssFeedService] HTTP request: GET https://example.com/feed.xml
DEBUG [RssFeedService] HTTP response: 200 OK
DEBUG [RssFeedService] Parsing RSS feed with 5 items
DEBUG [RssFeedService] Applied filters: maxItems=3, newerThan=2024-01-01T00:00:00Z
DEBUG [RssFeedService] Filtered to 2 items
```

## Testing and Validation

### Test with Known Working Feeds

```json
{
  "feedUrl": "https://feeds.bbci.co.uk/news/rss.xml",
  "maxItems": 5
}
```

### Test Authentication

```bash
# Test basic authentication
curl -u username:password https://private-feed.example.com/rss.xml

# Test bearer token
curl -H "Authorization: Bearer your-token" https://api.example.com/feed
```

### Validate RSS Feed

```bash
# Check feed validity
curl -s "https://example.com/feed.xml" | xmllint --format -

# Check feed content
curl -s "https://example.com/feed.xml" | grep -i "item"
```

## Performance Optimization

### Connection Pooling

```java
// Configure HTTP client for optimal performance
OkHttpClient client = new OkHttpClient.Builder()
    .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES))
    .build();
```

### Caching

```java
// Implement caching for frequently accessed feeds
Cache cache = new Cache(cacheDirectory, cacheSize);
OkHttpClient client = new OkHttpClient.Builder()
    .cache(cache)
    .build();
```

### Rate Limiting

```java
// Implement rate limiting
RateLimiter rateLimiter = RateLimiter.create(10.0); // 10 requests per second
```

## Error Codes and Messages

### Common Error Messages

| Error Message | Cause | Solution |
|---------------|-------|----------|
| "feedUrl is required" | Missing feed URL | Provide valid feed URL |
| "HTTP request failed with status 404" | Feed not found | Check URL and feed availability |
| "Failed to parse RSS feed" | Invalid XML | Validate feed format |
| "Authentication failed" | Invalid credentials | Check auth type and token |
| "Connection timeout" | Network issues | Check connectivity and timeouts |

### HTTP Status Codes

| Status Code | Meaning | Action |
|-------------|---------|--------|
| 200 | Success | Continue processing |
| 301/302 | Redirect | Follow redirects |
| 401 | Unauthorized | Check authentication |
| 403 | Forbidden | Check permissions |
| 404 | Not Found | Verify feed URL |
| 500 | Server Error | Contact feed provider |

## Support and Resources

### Getting Help

1. **Check Logs**: Review connector logs for error details
2. **Test Feed**: Verify feed works in browser or curl
3. **Documentation**: Review this troubleshooting guide
4. **Community**: Ask questions in Camunda Community Forum
5. **Support**: Contact Camunda Support for enterprise issues

### Useful Tools

- **RSS Validator**: https://validator.w3.org/feed/
- **XML Validator**: https://www.xmlvalidation.com/
- **HTTP Testing**: curl, Postman, or similar tools
- **Network Testing**: ping, traceroute, telnet

### Common RSS Feed URLs for Testing

- BBC News: `https://feeds.bbci.co.uk/news/rss.xml`
- TechCrunch: `https://techcrunch.com/feed/`
- Reddit: `https://www.reddit.com/r/programming.rss`
- GitHub: `https://github.com/trending.atom`

This troubleshooting guide should help resolve most common issues with the RSS Feed Connector. For additional support, please refer to the Camunda documentation or contact support.
