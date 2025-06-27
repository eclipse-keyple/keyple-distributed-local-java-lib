# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Migrated the CI pipeline from Jenkins to GitHub Actions.

## [2.5.2] - 2025-01-17
### Fixed
- Fixed the JSON format of exceptions exchanged over the network.

## [2.5.1] - 2024-09-19
### Fixed
- Fixed the backward compatibility for clients that do not transmit the `apiLevel` field
  (issue [eclipse-keyple/keyple-distributed-remote-java-lib#15]).
### Upgraded
- Keyple Distributed Network Lib `2.5.0` -> `2.5.1`

## [2.5.0] - 2024-09-06
### Added
- Optimizes the "Reader Client Side" usage mode. 
  When a remote service is requested, 
  the client sends to the server the information on whether the local reader is in contact or contactless mode. 
  This reduces the number of network exchanges.
  This optimization will only be effective if the server is running version `2.5+` of the 
  [Keyple Distributed Remote Library](https://keyple.org/components-java/distributed/keyple-distributed-remote-java-lib/).
### Changed
- Logging improvement.
### Upgraded
- Keyple Distributed Local API `2.1.1` -> `2.2.0`
- Keyple Distributed Network Lib `2.3.1` -> `2.5.0`

## [2.3.1] - 2024-04-12
### Changed
- Java source and target levels `1.6` -> `1.8`
### Upgraded
- Keyple Common API `2.0.0` -> `2.0.1`
- Keyple Distributed Local API `2.1.0` -> `2.1.1`
- Keyple Distributed Network Lib `2.3.0` -> `2.3.1`
- Keyple Util Lib `2.3.1` -> `2.4.0`
- Gradle `6.8.3` -> `7.6.4`

## [2.3.0] - 2023-11-28
### Added
- Added a property indicating the Distributed JSON API level in exchanged JSON data (current value: `"apiLevel": 2`).
- Added project status badges on `README.md` file.
### Fixed
- CI: code coverage report when releasing.
### Upgraded
- Keyple Distributed Local API `2.0.0` -> `2.1.0`
- Keyple Distributed Network Library `2.2.0` -> `2.3.0`
- Keyple Util Library `2.3.0` -> `2.3.1` (source code not impacted)

## [2.2.0] - 2023-04-04
:warning: **CAUTION**: this version requires to use at least version `2.2.0` of the
[Keyple Distributed Remote Library](https://keyple.org/components-java/distributed/keyple-distributed-remote-java-lib/)!
### Added
- `CHANGELOG.md` file (issue [eclipse-keyple/keyple#6]).
- CI: Forbid the publication of a version already released (issue [#5]).
### Changed
- Initial card content and user input/output data used for "ReaderClientSide" mode are now serialized/de-serialized 
  as JSON objects, and no more as strings containing JSON objects.
- All JSON property names are now "lowerCamelCase" formatted.
### Upgraded
- "Keyple Distributed Network Library" to version `2.2.0`.
- "Keyple Util Library" to version `2.3.0`.
- "Google Gson Library" (com.google.code.gson) to version `2.10.1`.

## [2.0.0] - 2021-10-06
This is the initial release.
It follows the extraction of Keyple 1.0 components contained in the `eclipse-keyple/keyple-java` repository to dedicated repositories.
It also brings many major API changes.

[unreleased]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/compare/2.5.2...HEAD
[2.5.2]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/compare/2.5.1...2.5.2
[2.5.1]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/compare/2.5.0...2.5.1
[2.5.0]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/compare/2.3.1...2.5.0
[2.3.1]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/compare/2.3.0...2.3.1
[2.3.0]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/compare/2.2.0...2.3.0
[2.2.0]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/compare/2.0.0...2.2.0
[2.0.0]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/releases/tag/2.0.0

[#5]: https://github.com/eclipse-keyple/keyple-distributed-local-java-lib/issues/5

[eclipse-keyple/keyple-distributed-remote-java-lib#15]: https://github.com/eclipse-keyple/keyple-distributed-remote-java-lib/issues/15

[eclipse-keyple/keyple#6]: https://github.com/eclipse-keyple/keyple/issues/6