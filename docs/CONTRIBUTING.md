# Contributing to ScanMonitorApps

## Introduction

Thank you for your interest in contributing to ScanMonitorApps! This document provides guidelines and instructions for contributing to the project. ScanMonitorApps is a lightweight Android mobile application designed to help Jump staff monitor ticket scanner activities during sports games. Your contributions help improve this tool for operations staff.

## Code of Conduct

We expect all contributors to adhere to professional standards of behavior. Please be respectful and constructive in all communications and contributions. Harassment, offensive comments, or any form of discrimination will not be tolerated.

## Getting Started

Before you begin contributing, please make sure you have set up your development environment and familiarized yourself with the project structure.

### Development Environment Setup

Please follow the [Development Environment Setup Guide](setup/development_environment.md) for detailed instructions on setting up your local development environment, including installing required software and configuring API keys.

### Build Instructions

Refer to the [Build Instructions](setup/build_instructions.md) for information on how to build and run the application locally.

### Understanding the Architecture

To understand the system design and component relationships, please review the [High-Level Architecture Documentation](architecture/high_level_architecture.md). Familiarizing yourself with the MVVM architecture pattern used in this project will help you contribute effectively.

## Development Workflow

We follow a standard GitHub workflow with feature branches and pull requests.

### Branching Strategy

- `main` - The main branch containing stable, production-ready code
- `feature/*` - Feature branches for new functionality
- `fix/*` - Bug fix branches
- `docs/*` - Documentation update branches

Always create a new branch from the latest `main` for your changes.

### Commit Messages

Write clear, concise commit messages that explain the purpose of your changes. Follow these guidelines:

- Start with a verb in the imperative mood (e.g., "Add", "Fix", "Update")
- Keep the first line under 72 characters
- Reference issue numbers when applicable (e.g., "Fix #123: Resolve offline mode data display issue")
- For complex changes, include a more detailed description after the summary line

### Pull Requests

When your changes are ready for review:

1. Push your branch to GitHub
2. Create a pull request targeting the `main` branch
3. Complete all sections of the [pull request template](../github/PULL_REQUEST_TEMPLATE.md)
4. Request review from appropriate team members
5. Address any feedback from reviewers

All pull requests must pass automated checks and receive approval from at least one maintainer before merging.

## Coding Standards

Following consistent coding standards ensures code quality and maintainability.

### Kotlin Style Guide

We follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). The project uses [detekt](https://detekt.dev/) for static code analysis, which enforces these standards.

### Android Guidelines

- Follow Android's [core app quality guidelines](https://developer.android.com/docs/quality-guidelines/core-app-quality)
- Use Android Jetpack components appropriately
- Implement proper error handling and offline functionality
- Consider stadium environment constraints (variable lighting, network conditions)

### Testing Requirements

All code changes should include appropriate tests:

- Unit tests for business logic (aim for 80% coverage)
- Integration tests for component interactions
- UI tests for critical user flows

Run all tests locally before submitting your pull request to ensure they pass.

### Documentation

- Add/update KDoc comments for all public functions, classes, and properties
- Update relevant documentation files when changing functionality
- Include code comments for complex or non-obvious implementations

## Issue Reporting

If you find a bug or have a feature request, please create an issue using the appropriate template:

### Bug Reports

Use the [Bug Report template](../github/ISSUE_TEMPLATE/bug_report.md) and include:

- Clear steps to reproduce
- Expected vs. actual behavior
- Device and Android version information
- Screenshots or videos if applicable

### Feature Requests

Use the [Feature Request template](../github/ISSUE_TEMPLATE/feature_request.md) and include:

- Clear description of the proposed feature
- Rationale and use cases
- Any implementation ideas you may have

## Testing

Testing is a critical part of the development process. The application must perform reliably in stadium environments with variable network conditions and lighting.

### Running Tests

Run tests locally before submitting changes:

```bash
# Unit tests
./gradlew testDebugUnitTest

# Android instrumented tests (requires connected device)
./gradlew connectedDebugAndroidTest

# Static analysis
./gradlew detekt
./gradlew lint
```

### Test Coverage

Focus on testing:

- Critical paths (data retrieval, display, refresh mechanisms)
- Error scenarios (API errors, network issues)
- Offline functionality
- UI state transitions

### Stadium Environment Testing

When possible, test your changes in conditions similar to a stadium environment:

- Variable network connectivity
- Different lighting conditions
- High-traffic periods

Document your testing approach in your pull request.

## CI/CD Pipeline

We use GitHub Actions for continuous integration. When you submit a pull request, automated workflows will run to verify your changes.

### Automated Checks

Pull requests trigger the following automated checks:

- Build verification
- Unit and instrumented tests
- Static code analysis (detekt, Android lint)
- Code style verification

### Addressing CI Failures

If CI checks fail, you will need to address the issues before your pull request can be merged. Check the workflow logs for details on any failures.

## Release Process

The release process is managed by project maintainers. Releases follow semantic versioning (MAJOR.MINOR.PATCH) and are distributed to Jump staff through Google Play Internal Testing. Contributors don't need to handle releases, but should be aware of the versioning scheme when documenting changes.

## Community and Communication

- **Questions and Discussions**: Use GitHub Issues for questions related to the code or implementation
- **Code Reviews**: Be respectful and constructive in code review comments
- **Acknowledgments**: Contributors will be acknowledged in the project documentation

## License

By contributing to this project, you agree that your contributions will be licensed under the same license as the project. This is a proprietary application for internal Jump use only.

## Thank You

Thank you for contributing to ScanMonitorApps! Your efforts help improve the experience for Jump staff during game operations.