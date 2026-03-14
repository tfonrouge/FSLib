# Contributing to FSLib

Thank you for your interest in contributing to FSLib!

## Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a feature branch from `master`
4. Make your changes

## Development Setup

- **JDK 21** or higher
- **MongoDB** (if working on the `:mongodb` module)

```bash
./gradlew build          # Build all modules
./gradlew :core:allTests # Run core tests
./gradlew :ssr:test      # Run SSR tests
```

## Guidelines

- Add KDoc comments to all public APIs
- Use `fieldName(Model::property)` for Tabulator column fields
- Write code comments and user-facing strings in English
- Follow existing patterns in the codebase

## Pull Requests

1. Ensure `./gradlew build` passes
2. Keep PRs focused on a single change
3. Write a clear description of what and why

## Reporting Issues

Open an issue at https://github.com/tfonrouge/fsLib/issues with:
- Steps to reproduce
- Expected vs actual behavior
- FSLib version and Kotlin version

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
