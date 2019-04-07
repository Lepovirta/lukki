#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

BUILDINFO_PKG="github.com/Lepovirta/lukki/internal/buildinfo"
VERSION=$(git describe --match 'v[0-9]*' | sed 's/^v//')
PLATFORMS=(
    "linux/arm"
    "linux/arm64"
    "linux/386"
    "linux/amd64"
    "darwin/amd64"
    "windows/386"
    "windows/amd64"
)

build_go() {
    local suffix
    if [ "$GOOS" = "windows" ]; then
        suffix=".exe"
    fi
    go build -v \
        -ldflags "-X $BUILDINFO_PKG.Version=$VERSION" \
        -o "out/lukki-${GOOS}-${GOARCH}${suffix:-}"
}

build_all() {
    mkdir -p out
    local platform_split
    for platform in "${PLATFORMS[@]}"; do
        platform_split=(${platform//\// })
        GOOS=${platform_split[0]}
        GOARCH=${platform_split[1]}
        build_go
    done
}

build_all
