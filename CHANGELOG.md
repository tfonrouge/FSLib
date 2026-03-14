# Changelog

All notable changes to this project will be documented in this file.

## [3.0.3] - 2026-03-14

### Changed
- Maven groupId changed from `io.github.tfonrouge.fslib` to `com.fonrouge.fslib`
- Replace `fslib-named-routes` Gradle plugin with Kilua RPC's built-in `@RpcBindingRoute` annotation
- Update documentation with Android sample link

### Removed
- `fslib-named-routes.gradle.kts` convention plugin (no longer needed)
- Migration guides (`MIGRATION-GUIDE-2.0.md`, `MIGRATION-GUIDE-3.0.md`)

## [3.0.2] - 2026-03-13

### Added
- Repository and website links for external dependencies in docs
- Updated README.md and USAGE-GUIDE.md

## [3.0.1] - 2026-03-12

### Added
- Named routes for Kilua RPC via `fslib-named-routes` Gradle plugin
- `RouteContract` class for API contract endpoint (`/apiContract`)
- `InMemoryRepository` for samples, tests, and prototyping (`:memorydb` module)
- Showcase sample with shared contract library pattern

## [3.0.0] - 2026-03-10

### Changed
- Module renames: `:base` to `:core`, `:fullStack` to `:fullstack`, `:utils` to `:media`
- Extracted MongoDB and SQL into independent engine modules (`:mongodb`, `:sql`)
- Decoupled permission system via `IRolePermissionProvider` / `PermissionRegistry`
- Migrated from KVision RPC to Kilua RPC

### Added
- `:sql` module with `SqlRepository` implementation using Exposed
- `:memorydb` module for in-memory storage
- `:ssr` module for server-side rendering with Ktor HTML builder
- Cross-engine dependency checking between MongoDB and SQL repositories
