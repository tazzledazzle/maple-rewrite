#!/usr/bin/env bash
set -euo pipefail

# Local-only repo generator
# Usage:
#   ./scripts/fixtures_local.sh init 10 /tmp/maple-fixtures 1.0.0
#   ./scripts/fixtures_local.sh destroy /tmp/maple-fixtures

cmd=${1:-}

init_repos () {
  count=$1
  root=$2
  version=$3
  prefix=${4:-maple-fixture-}

  mkdir -p "$root"
  bom="$root/fixtures-bom.json"
  echo "[" > "$bom"

  for i in $(seq -f "%02g" 1 "$count"); do
    name="${prefix}${i}"
    dir="$root/$name"
    mkdir -p "$dir/src/main/kotlin" "$dir/src/test/kotlin"
    cat > "$dir/build.gradle.kts" <<'EOF'
plugins { kotlin("jvm") version "1.9.25" }
repositories { mavenCentral() }
dependencies { testImplementation(kotlin("test")) }
tasks.test { useJUnitPlatform() }
EOF
    echo 'fun main() = println("Hello from fixture")' > "$dir/src/main/kotlin/Main.kt"

    pushd "$dir" >/dev/null
      git init -q
      git add .
      git commit -m "init $name" -q
      git tag "$version"
    popd >/dev/null

    echo "  {\"name\":\"$name\",\"version\":\"$version\",\"repoUrl\":\"file://$dir\",\"firstParty\":true,\"buildCmd\":\"./gradlew build\"}," >> "$bom"
  done

  # strip last comma (macOS vs GNU sed)
  sed -i '' -e '$ s/,$//' "$bom" 2>/dev/null || sed -i -e '$ s/,$//' "$bom"
  echo "]" >> "$bom"
  echo "Local fixtures created under $root"
  echo "BOM: $bom"
}

destroy_repos () {
  root=$1
  rm -rf "$root"
  echo "Deleted $root"
}

case "$cmd" in
  init)
    init_repos "${2:-5}" "${3:-/tmp/maple-fixtures}" "${4:-1.0.0}" "${5:-maple-fixture-}"
    ;;
  destroy)
    destroy_repos "${2:-/tmp/maple-fixtures}"
    ;;
  *)
    echo "Usage:
  $0 init <count> <root> <version> [prefix]
  $0 destroy <root>"
    exit 1
    ;;
esac