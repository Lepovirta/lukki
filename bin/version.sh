#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
git describe --match 'v[0-9]*' | sed 's/^v//'
