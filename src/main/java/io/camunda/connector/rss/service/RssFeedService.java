package io.camunda.connector.rss.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.camunda.connector.rss.dto.RssEnclosure;
import io.camunda.connector.rss.dto.RssFeedInput;
import io.camunda.connector.rss.dto.RssFeedOutput;
import io.camunda.connector.rss.dto.RssItem;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service class for fetching and parsing RSS feeds
 */
public class RssFeedService {
    
    private static final Logger LOG = LoggerFactory.getLogger(RssFeedService.class);

    // Rate limiting configuration - configurable via environment variables
    private static final int MAX_REQUESTS_PER_MINUTE = getEnvInt("RSS_CONNECTOR_MAX_REQUESTS_PER_MINUTE", 60);
    private static final int MAX_REQUESTS_PER_HOUR = getEnvInt("RSS_CONNECTOR_MAX_REQUESTS_PER_HOUR", 1000);
    private static final ConcurrentHashMap<String, AtomicInteger> requestCountsMinute = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicInteger> requestCountsHour = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> lastRequestTimeMinute = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, AtomicLong> lastRequestTimeHour = new ConcurrentHashMap<>();

    // HTTP client cache for reuse with LRU eviction policy
    private static final int MAX_CLIENT_CACHE_SIZE = 100;
    private static final Map<String, OkHttpClient> clientCache = new LinkedHashMap<String, OkHttpClient>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, OkHttpClient> eldest) {
            return size() > MAX_CLIENT_CACHE_SIZE;
        }
    };

    public RssFeedService() {
    }

    public RssFeedService(OkHttpClient httpClient) {
        // Constructor for testing with custom HTTP client
        // The httpClient parameter is used for test injection
    }

    /**
     * Helper method to read integer from environment variable with default value
     */
    private static int getEnvInt(String envVar, int defaultValue) {
        String value = System.getenv(envVar);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                LOG.warn("Invalid integer value for {}: {}, using default: {}", envVar, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * Fetch RSS feed from the specified URL
     */
    public RssFeedOutput fetchRssFeed(RssFeedInput input) {
        LOG.info("Fetching RSS feed from: {}", input.getFeedUrl());
        
        try {
            // Validate URL to prevent SSRF attacks
            validateUrl(input.getFeedUrl());
            
            // Check rate limiting
            checkRateLimit(input.getFeedUrl());
            
            // Get or create HTTP client with proper configuration (reuse for performance)
            OkHttpClient client = getOrCreateHttpClient(input);
            
            // Create HTTP request
            Request request = createHttpRequest(input);
            
            // Execute HTTP request with retry logic
            return executeWithRetry(client, request, input, 3);
            
        } catch (Exception e) {
            String errorMsg = "Failed to fetch RSS feed: " + e.getMessage();
            LOG.error(errorMsg, e);
            return new RssFeedOutput(false, errorMsg);
        }
    }
    
    private Request createHttpRequest(RssFeedInput input) {
        Request.Builder requestBuilder = new Request.Builder()
                .url(input.getFeedUrl())
                .addHeader("User-Agent", input.getUserAgent());
        
        // Add authentication if specified
        if (input.getAuthType() != null && !input.getAuthType().isEmpty() && 
            input.getAuthToken() != null && !input.getAuthToken().isEmpty()) {
            
            if ("basic".equalsIgnoreCase(input.getAuthType())) {
                // Basic authentication - split username:password from authToken
                String[] parts = input.getAuthToken().split(":", 2);
                String username = parts.length > 0 ? parts[0] : "";
                String password = parts.length > 1 ? parts[1] : "";
                String credentials = okhttp3.Credentials.basic(username, password);
                requestBuilder.addHeader("Authorization", credentials);
            } else if ("bearer".equalsIgnoreCase(input.getAuthType())) {
                // Bearer token authentication
                requestBuilder.addHeader("Authorization", "Bearer " + input.getAuthToken());
            }
        }
        
        return requestBuilder.build();
    }
    
    private RssFeedOutput parseRssFeed(String xmlContent, RssFeedInput input) {
        try {
            // Set XXE protection system properties
            System.setProperty("javax.xml.accessExternalDTD", "none");
            System.setProperty("javax.xml.accessExternalSchema", "none");
            
            SyndFeedInput syndFeedInput = new SyndFeedInput();
            InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
            
            // Create XML reader with XXE protection
            XmlReader xmlReader = new XmlReader(inputStream);
            
            SyndFeed syndFeed = syndFeedInput.build(xmlReader);
            
            RssFeedOutput output = new RssFeedOutput();
            output.setFeedTitle(syndFeed.getTitle());
            output.setFeedDescription(syndFeed.getDescription());
            output.setFeedLink(syndFeed.getLink());
            output.setFeedLanguage(syndFeed.getLanguage());
            
            // Parse and filter items
            List<RssItem> items = parseRssItems(syndFeed.getEntries(), input);
            output.setItems(items);
            
            LOG.info("Successfully parsed RSS feed with {} items", items.size());
            return output;
            
        } catch (Exception e) {
            String errorMsg = "Failed to parse RSS feed: " + e.getMessage();
            LOG.error(errorMsg, e);
            return new RssFeedOutput(false, errorMsg);
        }
    }
    
    private List<RssItem> parseRssItems(List<SyndEntry> entries, RssFeedInput input) {
        List<RssItem> items = new ArrayList<>();
        Instant newerThanInstant = parseNewerThanTimestamp(input.getNewerThan());
        
        for (SyndEntry entry : entries) {
            // Apply max items limit
            if (input.getMaxItems() != null && items.size() >= input.getMaxItems()) {
                break;
            }
            
            RssItem item = convertToRssItem(entry);
            
            // Apply GUID blacklist filter
            if (input.getGuidBlacklist() != null && 
                input.getGuidBlacklist().contains(item.getGuid())) {
                continue;
            }
            
            // Apply newer-than filter
            if (newerThanInstant != null && item.getPubDate() != null) {
                try {
                    Instant itemInstant = Instant.parse(item.getPubDate());
                    if (itemInstant.isBefore(newerThanInstant)) {
                        continue;
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to parse item pubDate: {}", item.getPubDate(), e);
                }
            }
            
            items.add(item);
        }
        
        return items;
    }
    
    private RssItem convertToRssItem(SyndEntry entry) {
        RssItem item = new RssItem();
        
        item.setTitle(entry.getTitle());
        item.setDescription(entry.getDescription() != null ? entry.getDescription().getValue() : null);
        item.setLink(entry.getLink());
        item.setGuid(entry.getUri());
        item.setAuthor(entry.getAuthor());
        item.setComments(entry.getComments());
        
        // Handle publication date
        if (entry.getPublishedDate() != null) {
            item.setPubDate(entry.getPublishedDate().toInstant().toString());
        } else if (entry.getUpdatedDate() != null) {
            item.setPubDate(entry.getUpdatedDate().toInstant().toString());
        }
        
        // Handle categories
        if (entry.getCategories() != null && !entry.getCategories().isEmpty()) {
            List<String> categories = new ArrayList<>();
            entry.getCategories().forEach(category -> categories.add(category.getName()));
            item.setCategories(categories);
        }
        
        // Handle enclosures
        if (entry.getEnclosures() != null && !entry.getEnclosures().isEmpty()) {
            List<RssEnclosure> enclosures = new ArrayList<>();
            for (SyndEnclosure enclosure : entry.getEnclosures()) {
                RssEnclosure rssEnclosure = new RssEnclosure(
                    enclosure.getUrl(),
                    enclosure.getType(),
                    enclosure.getLength()
                );
                enclosures.add(rssEnclosure);
            }
            item.setEnclosures(enclosures);
        }
        
        // Handle content
        if (entry.getContents() != null && !entry.getContents().isEmpty()) {
            StringBuilder contentBuilder = new StringBuilder();
            entry.getContents().forEach(content -> contentBuilder.append(content.getValue()));
            item.setContent(contentBuilder.toString());
        }
        
        return item;
    }
    
    private Instant parseNewerThanTimestamp(String newerThan) {
        if (newerThan == null || newerThan.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Instant.parse(newerThan);
        } catch (Exception e) {
            LOG.warn("Failed to parse newerThan timestamp: {}", newerThan, e);
            return null;
        }
    }
    
    /**
     * Create HTTP client with configuration from input parameters
     */
    private OkHttpClient createHttpClientWithConfig(RssFeedInput input) {
        int timeoutSeconds = input.getTimeoutSeconds() != null ? input.getTimeoutSeconds() : 30;
        boolean ignoreTls = input.getIgnoreTls() != null ? input.getIgnoreTls() : false;

        // Security check: Disable ignoreTls in production environments
        if (ignoreTls && isProductionEnvironment()) {
            LOG.error("SECURITY VIOLATION: ignoreTls=true is not allowed in production environments");
            throw new SecurityException("SSL certificate validation cannot be disabled in production environments");
        }

        return createHttpClientWithTimeout(timeoutSeconds, ignoreTls);
    }

    /**
     * Create HTTP client with timeout and SSL configuration
     * Consolidates SSL setup logic to avoid duplication
     */
    private OkHttpClient createHttpClientWithTimeout(int timeoutSeconds, boolean ignoreTls) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds, TimeUnit.SECONDS);

        if (ignoreTls) {
            configureTrustAllSsl(builder);
            LOG.warn("SSL certificate verification is disabled. This should only be used in development environments.");
        }

        return builder.build();
    }

    /**
     * Configure SSL to trust all certificates (for development/testing only)
     */
    private void configureTrustAllSsl(OkHttpClient.Builder builder) {
        try {
            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            // Create SSL context with trust-all manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create socket factory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

        } catch (Exception e) {
            LOG.error("Failed to configure SSL context for ignoreTls", e);
            throw new RuntimeException("Failed to configure SSL context", e);
        }
    }
    
    /**
     * Check if running in production environment
     */
    private boolean isProductionEnvironment() {
        String env = System.getProperty("camunda.environment", System.getenv("CAMUNDA_ENVIRONMENT"));
        return "production".equalsIgnoreCase(env) || "prod".equalsIgnoreCase(env);
    }
    
    /**
     * Validate URL to prevent SSRF attacks
     */
    private void validateUrl(String url) {
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String protocol = parsedUrl.getProtocol().toLowerCase();
            String host = parsedUrl.getHost().toLowerCase();
            
            // Only allow HTTP and HTTPS protocols
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new SecurityException("Only HTTP and HTTPS protocols are allowed");
            }
            
            // Block private/internal IP addresses
            if (isPrivateOrLocalAddress(host)) {
                throw new SecurityException("Access to private/internal networks is not allowed");
            }
            
            // Block localhost variations (IPv4 and IPv6)
            if (host.equals("localhost") || host.equals("127.0.0.1") || host.startsWith("127.") ||
                host.equals("::1") || host.equals("0:0:0:0:0:0:0:1") ||
                host.startsWith("192.168.") || host.startsWith("10.") ||
                host.startsWith("169.254.") || // Link-local
                isPrivateIpv4Range(host) ||
                isPrivateIpv6Range(host)) {
                throw new SecurityException("Access to private/internal networks is not allowed");
            }
            
        } catch (java.net.MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format", e);
        }
    }
    
    /**
     * Check if host is a private or local address
     */
    private boolean isPrivateOrLocalAddress(String host) {
        try {
            java.net.InetAddress address = java.net.InetAddress.getByName(host);
            return address.isLoopbackAddress() || address.isLinkLocalAddress() ||
                   address.isSiteLocalAddress() || address.isAnyLocalAddress();
        } catch (java.net.UnknownHostException e) {
            // If we can't resolve, allow it (might be a valid external host)
            return false;
        }
    }

    /**
     * Check if host is in private IPv4 range (172.16.0.0/12)
     */
    private boolean isPrivateIpv4Range(String host) {
        // Check 172.16.0.0 to 172.31.255.255 range
        if (host.startsWith("172.")) {
            String[] parts = host.split("\\.");
            if (parts.length >= 2) {
                try {
                    int secondOctet = Integer.parseInt(parts[1]);
                    return secondOctet >= 16 && secondOctet <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Check if host is in private IPv6 range
     */
    private boolean isPrivateIpv6Range(String host) {
        String lowerHost = host.toLowerCase();
        // fc00::/7 - Unique Local Addresses
        if (lowerHost.startsWith("fc") || lowerHost.startsWith("fd")) {
            return true;
        }
        // fe80::/10 - Link-Local addresses
        if (lowerHost.startsWith("fe80:") || lowerHost.startsWith("fe8") ||
            lowerHost.startsWith("fe9") || lowerHost.startsWith("fea") ||
            lowerHost.startsWith("feb")) {
            return true;
        }
        // ::1 - loopback (already checked above but adding for completeness)
        return false;
    }
    
    /**
     * Execute HTTP request with retry logic and exponential backoff
     */
    private RssFeedOutput executeWithRetry(OkHttpClient client, Request request, RssFeedInput input, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        String errorMsg = String.format("HTTP request failed with status %d: %s",
                                response.code(), response.message());
                        LOG.error(errorMsg);
                        return new RssFeedOutput(false, errorMsg);
                    }

                    // Parse response body with null check
                    if (response.body() == null) {
                        String errorMsg = "HTTP response body is null";
                        LOG.error(errorMsg);
                        return new RssFeedOutput(false, errorMsg);
                    }

                    String responseBody = response.body().string();
                    return parseRssFeed(responseBody, input);
                }
            } catch (Exception e) {
                lastException = e;
                
                if (attempt < maxRetries - 1) {
                    // Calculate exponential backoff delay
                    long delay = (long) Math.pow(2, attempt) * 1000; // 1s, 2s, 4s, etc.
                    LOG.warn("Request attempt {} failed, retrying in {}ms: {}", attempt + 1, delay, e.getMessage());
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Request interrupted", ie);
                    }
                } else {
                    LOG.error("All {} retry attempts failed", maxRetries, e);
                }
            }
        }
        
        String errorMsg = "Failed to fetch RSS feed after " + maxRetries + " attempts: " + 
                         (lastException != null ? lastException.getMessage() : "Unknown error");
        return new RssFeedOutput(false, errorMsg);
    }
    
    /**
     * Get or create HTTP client with caching for performance
     */
    private OkHttpClient getOrCreateHttpClient(RssFeedInput input) {
        // Create cache key based on configuration
        String cacheKey = String.format("%s_%s_%s", 
            input.getTimeoutSeconds() != null ? input.getTimeoutSeconds() : 30,
            input.getIgnoreTls() != null ? input.getIgnoreTls() : false,
            input.getUserAgent() != null ? input.getUserAgent() : "default");
        
        return clientCache.computeIfAbsent(cacheKey, k -> {
            LOG.debug("Creating new HTTP client for configuration: {}", cacheKey);
            return createHttpClientWithConfig(input);
        });
    }
    
    /**
     * Check rate limiting for the given URL (both per-minute and per-hour)
     */
    private void checkRateLimit(String url) {
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String host = parsedUrl.getHost();
            long currentTime = System.currentTimeMillis();

            // Check per-minute limit
            AtomicInteger countMinute = requestCountsMinute.computeIfAbsent(host, k -> new AtomicInteger(0));
            AtomicLong lastTimeMinute = lastRequestTimeMinute.computeIfAbsent(host, k -> new AtomicLong(0));

            // Reset counter if more than a minute has passed
            if (currentTime - lastTimeMinute.get() > 60000) { // 1 minute
                countMinute.set(0);
            }

            // Check per-minute limit
            if (countMinute.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                throw new SecurityException("Rate limit exceeded: too many requests to " + host +
                    " (" + countMinute.get() + " requests per minute, max: " + MAX_REQUESTS_PER_MINUTE + ")");
            }

            // Update last request time for minute window
            lastTimeMinute.set(currentTime);

            // Check per-hour limit
            AtomicInteger countHour = requestCountsHour.computeIfAbsent(host, k -> new AtomicInteger(0));
            AtomicLong lastTimeHour = lastRequestTimeHour.computeIfAbsent(host, k -> new AtomicLong(0));

            // Reset counter if more than an hour has passed
            if (currentTime - lastTimeHour.get() > 3600000) { // 1 hour
                countHour.set(0);
            }

            // Check per-hour limit
            if (countHour.incrementAndGet() > MAX_REQUESTS_PER_HOUR) {
                throw new SecurityException("Rate limit exceeded: too many requests to " + host +
                    " (" + countHour.get() + " requests per hour, max: " + MAX_REQUESTS_PER_HOUR + ")");
            }

            // Update last request time for hour window
            lastTimeHour.set(currentTime);

            LOG.debug("Rate limit check passed for {}: {} requests/min, {} requests/hour",
                host, countMinute.get(), countHour.get());

        } catch (java.net.MalformedURLException e) {
            LOG.warn("Could not parse URL for rate limiting: {}", url);
        }
    }
    
    /**
     * Create HTTP client with SSL configuration (for testing/backward compatibility)
     * @deprecated Use createHttpClientWithTimeout instead
     */
    @Deprecated
    public static OkHttpClient createHttpClientWithSslConfig(boolean ignoreTls) {
        // Delegate to instance method using a temporary service instance
        RssFeedService tempService = new RssFeedService();
        return tempService.createHttpClientWithTimeout(30, ignoreTls);
    }
}
