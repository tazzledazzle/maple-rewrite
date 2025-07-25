# Maple

A release branching tool for automating consistent release branching/tagging across many first-party repositories using a single source of truth (BOM).

## Overview

Maple automates the tedious and error-prone process of creating release branches and tags across multiple repositories. It reads a Bill of Materials (BOM) file, identifies first-party dependencies, and for each repository:

1. Clones/fetches the repository
2. Checks out the specified version
3. Builds and tests the project
4. Creates release branch and tag
5. Pushes changes to remote

## Features

- **BOM-driven**: Uses JSON BOM files as single source of truth
- **Parallel execution**: Processes multiple repositories concurrently
- **Resume capability**: Can resume failed runs from specific repositories
- **Dry-run mode**: Preview actions without making changes
- **Comprehensive reporting**: JSON and console output with detailed status
- **Pluggable architecture**: Extensible parsers and build runners
- **Observability**: Prometheus metrics and structured logging

## Quick Start

### Installation

Build from source:
```bash
git clone https://github.com/your-org/maple-rewrite.git
cd maple-rewrite
./gradlew build
```

### Basic Usage

1. **Plan a release** (dry-run):
```bash
./gradlew run --args="plan --bom bom.json --version 2024.1"
```

2. **Execute release**:
```bash
./gradlew run --args="run --bom bom.json --version 2024.1"
```

3. **Resume failed run**:
```bash
./gradlew run --args="resume --run-id abc123 --from-repo my-service"
```

### BOM File Format

```json
{
  "bomFormat": "CycloneDX",
  "specVersion": "1.4",
  "version": 1,
  "components": [
    {
      "type": "library",
      "bom-ref": "com.tableau.modules:my-service:1.0.0",
      "group": "com.tableau.modules",
      "name": "my-service",
      "version": "1.0.0",
      "purl": "pkg:maven/com.tableau.modules/my-service@1.0.0"
    }
  ],
  "dependencies": [
    {
      "ref": "com.tableau.modules:my-service:1.0.0",
      "dependsOn": ["other-dependency"]
    }
  ]
}
```

## Commands

### `plan`
Shows what would be done without executing:
```bash
maple plan --bom bom.json --version 2024.1
```

### `run`
Executes the release process:
```bash
maple run --bom bom.json --version 2024.1 [--dry-run] [--concurrency 4]
```

### `resume`
Resumes a failed run:
```bash
maple resume --run-id <id> --from-repo <repo-name>
```

### `fixtures`
Manages test fixtures:
```bash
maple fixtures init --remote local --count 3 --root /tmp/test-repos
maple fixtures destroy --remote local --root /tmp/test-repos
```

### `query`
Queries run history:
```bash
maple query last-run
```

## Configuration

Create `.maple.yml` in your project root:

```yaml
# Default configuration
workDir: .maple
concurrency: 4
timeout: 15m
firstPartyPrefix: com.tableau
buildCommand: ./gradlew build
branchTemplate: release/{version}
tagTemplate: {version}

# Repository overrides
repos:
  my-special-service:
    buildCommand: make build
    timeout: 30m
```

## Development

### Prerequisites
- Java 21+
- Git
- Docker (optional, for containerized builds)

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew test
```

### Running locally
```bash
./gradlew run --args="--help"
```

## Architecture

Maple follows a modular architecture:

- **CLI**: Command-line interface and argument parsing
- **BOM Parser**: Parses various BOM formats (JSON, future: Gradle catalogs)
- **Orchestrator**: Coordinates parallel execution and state management
- **Git Driver**: Handles git operations (clone, checkout, branch, tag, push)
- **Build Runner**: Executes build commands with timeout and logging
- **State Store**: Persists run state for resume capability
- **Reporter**: Generates JSON and console reports

## Observability

### Metrics
Enable Prometheus metrics:
```bash
export MAPLE_METRICS_ENABLED=true
export MAPLE_METRICS_PORT=9404
```

### Logging
Enable debug logging:
```bash
export MAPLE_DEBUG=true
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Run `./gradlew build` to ensure everything passes
6. Submit a pull request

## License

[Add your license here]

## Support

For issues and questions:
- Create an issue in this repository
- Check the [documentation](docs/)
- Review the [design document](docs/design.md)

