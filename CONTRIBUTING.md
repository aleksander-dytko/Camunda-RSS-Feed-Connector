# Contributing to RSS Feed Connector

Thank you for your interest in contributing to the RSS Feed Connector! This document provides guidelines for contributing to the project.

## How to Contribute

### 1. Fork and Clone

```bash
# Fork the repository on GitHub
# Then clone your fork
git clone https://github.com/your-username/rss-feed-connector.git
cd rss-feed-connector
```

### 2. Set Up Development Environment

**Prerequisites:**
- Java 17 or higher
- Maven 3.6 or higher
- Git

**Setup:**
```bash
# Install dependencies
mvn clean install

# Run tests to verify setup
./run-tests.sh
```

### 3. Create a Feature Branch

```bash
# Create and switch to a new branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/issue-description
```

### 4. Make Your Changes

**Code Style:**
- Follow Java coding conventions
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods small and focused

**Testing:**
- Add unit tests for new functionality
- Update existing tests if needed
- Ensure all tests pass

**Documentation:**
- Update README.md if adding new features
- Add examples for new functionality
- Update troubleshooting guide if needed

### 5. Test Your Changes

```bash
# Run all tests
./run-tests.sh

# Run specific tests
mvn test -Dtest="YourTestClass"

# Run with integration tests
./run-tests.sh --integration
```

### 6. Commit Your Changes

```bash
# Stage your changes
git add .

# Commit with a descriptive message
git commit -m "Add feature: brief description of changes"

# Push to your fork
git push origin feature/your-feature-name
```

### 7. Create a Pull Request

1. Go to your fork on GitHub
2. Click "New Pull Request"
3. Fill out the PR template
4. Submit the pull request

## Pull Request Guidelines

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] All tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No breaking changes (or documented)
```

### Review Process

1. **Automated Checks**: All CI checks must pass
2. **Code Review**: At least one maintainer must approve
3. **Testing**: All tests must pass
4. **Documentation**: Documentation must be updated

## Development Guidelines

### Code Style

**Java Code:**
```java
// Use meaningful names
public RssFeedOutput fetchRssFeed(RssFeedInput input) {
    // Add Javadoc comments
    /**
     * Fetches RSS feed from the specified URL
     * @param input RSS feed input parameters
     * @return RSS feed output with items
     */
}

// Use proper error handling
try {
    return processRssFeed(input);
} catch (Exception e) {
    LOG.error("Failed to process RSS feed", e);
    return new RssFeedOutput(false, "Processing failed");
}
```

**Testing:**
```java
@Test
void shouldFetchRssFeedSuccessfully() {
    // Given
    RssFeedInput input = createValidInput();
    
    // When
    RssFeedOutput output = connector.execute(context);
    
    // Then
    assertThat(output.isSuccess()).isTrue();
    assertThat(output.getItems()).isNotEmpty();
}
```

### Project Structure

```
src/
├── main/java/
│   └── io/camunda/connector/rss/
│       ├── RssFeedConnector.java
│       ├── dto/
│       └── service/
└── test/java/
    └── io/camunda/connector/rss/
        ├── RssFeedConnectorTest.java
        ├── service/
        └── integration/
```

### Adding New Features

1. **Create Issue**: Document the feature request
2. **Design**: Plan the implementation
3. **Implement**: Write code and tests
4. **Document**: Update documentation
5. **Test**: Ensure all tests pass
6. **Submit**: Create pull request

### Bug Reports

When reporting bugs, please include:

1. **Description**: Clear description of the issue
2. **Steps to Reproduce**: Detailed steps
3. **Expected Behavior**: What should happen
4. **Actual Behavior**: What actually happens
5. **Environment**: Java version, Maven version, OS
6. **Logs**: Relevant error messages or logs

### Feature Requests

When requesting features, please include:

1. **Use Case**: Why is this feature needed?
2. **Proposed Solution**: How should it work?
3. **Alternatives**: Other approaches considered
4. **Additional Context**: Any other relevant information

## Testing Guidelines

### Unit Tests

```java
@Test
void shouldValidateInputCorrectly() {
    // Test input validation
    RssFeedInput input = new RssFeedInput();
    input.setFeedUrl(null);
    
    assertThatThrownBy(() -> connector.execute(context))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("feedUrl is required");
}
```

### Integration Tests

```java
@Test
@EnabledIfEnvironmentVariable(named = "RUN_INTEGRATION_TESTS", matches = "true")
void shouldFetchRealRssFeed() {
    // Test with real RSS feed
    RssFeedInput input = new RssFeedInput();
    input.setFeedUrl("https://feeds.bbci.co.uk/news/rss.xml");
    
    RssFeedOutput output = connector.execute(context);
    assertThat(output.isSuccess()).isTrue();
}
```

### Test Coverage

- Aim for >80% code coverage
- Test all public methods
- Test error scenarios
- Test edge cases

## Documentation Guidelines

### README Updates

When adding new features:

1. Update the feature list
2. Add usage examples
3. Update the API documentation
4. Add troubleshooting information

### Code Documentation

```java
/**
 * Fetches RSS feed from the specified URL with optional filtering
 * 
 * @param input RSS feed input parameters
 * @return RSS feed output with parsed items
 * @throws IllegalArgumentException if input validation fails
 * @throws IOException if network request fails
 */
public RssFeedOutput fetchRssFeed(RssFeedInput input) throws Exception {
    // Implementation
}
```

## Release Process

### Version Numbering

We use semantic versioning (MAJOR.MINOR.PATCH):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist

- [ ] All tests pass
- [ ] Documentation updated
- [ ] Version number updated
- [ ] Changelog updated
- [ ] Release notes prepared

## Community Guidelines

### Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Help others learn and grow
- Follow the golden rule

### Getting Help

- **GitHub Issues**: For bug reports and feature requests
- **GitHub Discussions**: For questions and general discussion
- **Camunda Community**: For Camunda-specific questions

### Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- Camunda Community highlights

## License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

## Questions?

If you have questions about contributing, please:

1. Check the existing issues and discussions
2. Create a new issue with the "question" label
3. Join the Camunda Community Forum
4. Contact the maintainers

Thank you for contributing to the RSS Feed Connector! 🚀
