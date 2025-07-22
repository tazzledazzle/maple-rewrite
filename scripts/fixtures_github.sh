#!/usr/bin/env bash
set -euo pipefail

# GitHub-backed repo generator using gh CLI
# Usage:
#   ./scripts/fixtures_github.sh init 5 tazzledazzle maple-fixture- 1.0.0
#   ./scripts/fixtures_github.sh destroy tazzledazzle maple-fixture-

cmd=${1:-}

require_gh () {
  if ! command -v gh >/dev/null; then
    echo "gh CLI not found. Install: https://cli.github.com/"
    exit 1
  fi
}

init_repos () {
  count=$1
  org=$2
  prefix=$3
  version=$4
  temp_root=${5:-/tmp/maple-fixtures-github}

  require_gh
  mkdir -p "$temp_root"
  bom="$temp_root/fixtures-bom.json"
  echo "[" > "$bom"

  for i in $(seq -f "%02g" 1 "$count"); do
    name="${prefix}${i}"
    full="$org/$name"

    echo "Creating $full"
    gh repo create "$full" --private --disable-issues --disable-wiki -y >/dev/null
    dir="$temp_root/$name"
    mkdir -p "$dir/src/main/kotlin" "$dir/src/test/kotlin"

    cat > "$dir/build.gradle.kts" <<'EOF'
plugins { kotlin("jvm") version "1.9.25" }
repositories { mavenCentral() }
dependencies { testImplementation(kotlin("test")) }
tasks.test { useJUnitPlatform() }
EOF
    echo 'fun main() = println("Hello from GH fixture")' > "$dir/src/main/kotlin/Main.kt"

    pushd "$dir" >/dev/null
      git init -q
      git remote add origin "git@github.com:$full.git"
      git add .
      git commit -m "init $name" -q
      git tag "$version"
      git push origin HEAD:main --tags -q
    popd >/dev/null

    repo_url="git@github.com:$full.git"
    echo "  {\"name\":\"$name\",\"version\":\"$version\",\"repoUrl\":\"$repo_url\",\"firstParty\":true,\"buildCmd\":\"./gradlew build\"}," >> "$bom"
  done

  sed -i '' -e '$ s/,$//' "$bom" 2>/dev/null || sed -i -e '$ s/,$//' "$bom"
  echo "]" >> "$bom"
  echo "GitHub fixtures created. BOM at $bom"
}

destroy_repos () {
  org=$1
  prefix=$2

  require_gh

  repos=$(gh repo list "$org" --limit 200 --json name --jq ".[] | select(.name|startswith(\"$prefix\")) | .name")
  if [[ -z "$repos" ]]; then
    echo "No repos with prefix $prefix under $org"
    exit 0
  fi

  echo "Deleting:"
  echo "$repos"
  for r in $repos; do
    gh repo delete "$org/$r" -y
  done
}

case "$cmd" in
  init)
    init_repos "${2:-5}" "${3:?org/user required}" "${4:-maple-fixture-}" "${5:-1.0.0}" "${6:-/tmp/maple-fixtures-github}"
    ;;
  destroy)
    destroy_repos "${2:?org/user required}" "${3:-maple-fixture-}"
    ;;
  *)
    echo "Usage:
  $0 init    <count> <org> [prefix] [version] [tmp_root]
  $0 destroy <org> [prefix]"
    exit 1
    ;;
esac