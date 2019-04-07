#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

BUILDINFO_PKG="github.com/Lepovirta/lukki/internal/buildinfo"
VERSION=$(git describe --match 'v[0-9]*' | sed 's/^v//')

go mod download
go build -v -ldflags "-X $BUILDINFO_PKG.Version=$VERSION" -o lukki
