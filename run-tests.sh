#!/bin/bash

# RSS Feed Connector Test Runner
# This script runs all tests for the RSS Feed Connector

set -e

echo "🚀 Starting RSS Feed Connector Tests"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven to run tests."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

print_success "Java version: $JAVA_VERSION"
print_success "Maven version: $(mvn -version | head -n 1)"

# Clean and compile
print_status "Cleaning and compiling project..."
mvn clean compile

if [ $? -eq 0 ]; then
    print_success "Compilation successful"
else
    print_error "Compilation failed"
    exit 1
fi

# Run unit tests
print_status "Running unit tests..."
mvn test

if [ $? -eq 0 ]; then
    print_success "Unit tests passed"
else
    print_error "Unit tests failed"
    exit 1
fi

# Check if integration tests should be run
if [ "$1" = "--integration" ] || [ "$1" = "-i" ]; then
    print_status "Running integration tests (requires internet connection)..."
    
    # Set environment variable for integration tests
    export RUN_INTEGRATION_TESTS=true
    
    # Run integration tests
    mvn test -Dtest="*IntegrationTest"
    
    if [ $? -eq 0 ]; then
        print_success "Integration tests passed"
    else
        print_warning "Integration tests failed (this may be due to network issues)"
    fi
else
    print_warning "Skipping integration tests. Use --integration flag to run them."
fi

# Generate test report
print_status "Generating test report..."
mvn jacoco:report
if [ $? -eq 0 ]; then
    print_success "Coverage report generated: target/site/jacoco/index.html"
else
    print_warning "Coverage report generation failed"
fi

# Package the connector
print_status "Packaging connector..."
mvn package

if [ $? -eq 0 ]; then
    print_success "Connector packaged successfully"
    print_status "JAR file location: target/rss-feed-connector-1.0.0.jar"
else
    print_error "Packaging failed"
    exit 1
fi

# Run specific test examples
print_status "Running example tests..."

# Test with BBC News feed
print_status "Testing with BBC News RSS feed..."
mvn test -Dtest="RssFeedConnectorIntegrationTest#shouldFetchRealRssFeed"

# Test with authentication
print_status "Testing authentication..."
mvn test -Dtest="RssFeedConnectorIntegrationTest#shouldHandleAuthentication"

print_success "All tests completed successfully!"
echo ""
echo "📋 Test Summary:"
echo "================"
echo "✅ Unit tests: PASSED"
if [ "$1" = "--integration" ] || [ "$1" = "-i" ]; then
    echo "✅ Integration tests: PASSED"
else
    echo "⏭️  Integration tests: SKIPPED (use --integration to run)"
fi
echo "✅ Packaging: PASSED"
echo ""
echo "🎉 RSS Feed Connector is ready for deployment!"
echo ""
echo "Next steps:"
echo "1. Deploy the JAR file to your Camunda Platform 8 environment"
echo "2. Configure the connector in your BPMN processes"
echo "3. Test with your specific RSS feeds"
echo ""
echo "For more information, see:"
echo "- README.md for usage instructions"
echo "- docs/ai-agent-integration.md for AI agent setup"
echo "- docs/troubleshooting.md for common issues"
