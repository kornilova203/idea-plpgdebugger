<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# idea-plpgdebugger Changelog

## [Unreleased]

## [1.1.1]
### Added
- Plugin configuration:
  - Custom query on both running and debugging session
  - Add SQL specific verbosity
### Changed
- Fix call identification
- Fix breakpoint support for PROCEDURE
- Improved breakpoint management

## [1.0.0-alpha]
### Added
- Allow direct debug of a PL/pg function
- View variables
- Explore arrays and composite type

## [1.0.0-alpha1]
### Added
- Fix procedure identification on specific schema
- Gracefully handle debug connection

## [1.0.0-beta]
### Added
- Support for breakpoint
- Implements a Virtual File System to store debug files

### Changed
- Refactor many parts for future test support

## [1.0.0-rc1]
### Changed
- Commands are within the PlExecutor
- Better verbosity
- Better use of coroutines
- Solve Prevent when extension is not avaible #12 (SQL issue)
- Progress on A routine with an enum in arg fails #13
- Fix compatibility with last DatabaseTool implementation

## [1.0.1]
### Changed
- Ignore case when for function detection
- Avoid JSON transformation error

## [1.0.2]
### Added
- Plugin configuration: timeouts and output verbosity

### Changed
- Fix procedure detection
- Many internal changes

## [1.0.3]
### Added
- Plugin configuration: 
  - Timeout control
  - Output verbosity
  - Internal queries(to fix manually)
  - Failure tests(for developer)

### Changed
- Fix procedure detection on PG 14
- Better error handling
- Close owner connection when something has failed

## [1.1.1]
### Added
- Plugin configuration:
  - Custom query on both running and debugging session
  - Add SQL specific verbosity

### Changed
- Fix call identification
- Fix breakpoint support for PROCEDURE
- Improved breakpoint management