# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- Project members added or removed through the project configuration members tab are now kept in sync with
  the IAM "projects" attribute of externally provisioned users (LS-AAI). Previously, their membership was
  removed as soon as they made their next request, because the reconciliation with IAM relied on the
  outdated "projects" claim embedded in the access token instead of the current IAM state
- Project members additions and removals that partially succeed are now explicitly reported (HTTP 206
  details surfaced in the UI instead of being treated as a full success)
- Failed project member operations now return a proper HTTP status code derived from the error
- Fix project representative handover trigger when an administrator loses consistent access to a project

## [5.2.0] - 2025-06-20

### Changed

- Refactor authentication process using IAM service with Keycloak

### Fixed

- Fix large files forwarding from and to the App Engine

### Removed

- Local authentication

## [5.1.1] - 2025-05-09

### Fixed

- Fix image input for App Engine to support both simple images and cropped regions defined by an annotation

## [5.1.0] - 2025-04-18

### Added

- App Engine service support
- Image group feature
- Annotation link feature
- Annotation group feature

### Changed

- Upgrade Spring Boot from 2.6.6 to 3.2.3
- Upgrade Gradle from 7.2 to 7.6
- Migrate from Javax to Jakarta

### Removed

- Legacy software support
- RabbitMQ message queue support

[Unreleased]: https://github.com/cytomine/Cytomine-core/compare/5.2.0..HEAD
[5.2.0]: https://github.com/cytomine/Cytomine-core/releases/tag/5.2.0
[5.1.1]: https://github.com/cytomine/Cytomine-core/releases/tag/5.1.1
[5.1.0]: https://github.com/cytomine/Cytomine-core/releases/tag/5.1.0
