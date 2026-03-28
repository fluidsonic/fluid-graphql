# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/).


## [Unreleased]

### Fixed

- Removed unnecessary non-null assertions and casts in spec test files to eliminate compilation warnings.
- Fixed Dokka documentation generation warnings by configuring cross-module link resolution.


## [0.16.0] - 2026-03-28

### Changed

- Migrated to fluid-gradle 3.0.0 (Kotlin 2.3.20, Gradle 9.4.1, JDK 21+).
- Updated GitHub Actions to latest versions.

### Added

- Comprehensive KDoc on all public API.
- Test coverage for DSL builders, execution conversion, and visitors.
