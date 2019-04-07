#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

BUILDINFO_PKG="github.com/Lepovirta/lukki/internal/buildinfo"
VERSION=$(./bin/version.sh)
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

create_shasums() {
    if [ -f out/SHA256SUMS ]; then
        rm out/SHA256SUMS
    fi
    (
        cd out/
        shasum -a 256 lukki* > SHA256SUMS
    )
}

build_all() {
    mkdir -p out
    echo "v$VERSION" > out/git_tag.txt
    for platform in "${PLATFORMS[@]}"; do
        GOOS=$(cut -d '/' -f1 <<< "$platform")
        GOARCH=$(cut -d '/' -f2 <<< "$platform")
        build_go
    done
    create_shasums
}

build_all
