# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
:warning: **CAUTION**: this version requires to use at least version `2.2.0` of the
[Keyple Distributed Remote Library](https://keyple.org/components-java/distributed/keyple-distributed-remote-java-lib/)!
### Added
- `CHANGELOG.md` file (issue [eclipse/keyple#6]).
- CI: Forbid the publication of a version already released (issue [#5]).
### Changed
- Initial card content and user input/output data used for "ReaderClientSide" mode are now serialized/de-serialized 
  as JSON objects, and no more as strings containing JSON objects.

## [2.0.0] - 2021-10-06
This is the initial release.
It follows the extraction of Keyple 1.0 components contained in the `eclipse/keyple-java` repository to dedicated repositories.
It also brings many major API changes.

[unreleased]: https://github.com/eclipse/keyple-distributed-local-java-lib/compare/2.0.0...HEAD
[2.0.0]: https://github.com/eclipse/keyple-distributed-local-java-lib/releases/tag/2.0.0

[#5]: https://github.com/eclipse/keyple-distributed-local-java-lib/issues/5

[eclipse/keyple#6]: https://github.com/eclipse/keyple/issues/6