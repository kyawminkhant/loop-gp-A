#!/bin/bash
# Starts the LOOP Team Hub on macOS and Linux.
# Windows users: run run-hub.cmd instead.

set -e
cd "$(dirname "$0")"

# Prefer a system Maven; fall back to the bundled wrapper.
if command -v mvn >/dev/null 2>&1; then
    MVN="mvn"
else
    MVN="sh modules/finance/mvnw"
fi

echo "Preparing all LOOP modules..."
$MVN -DskipTests install

echo "Starting the LOOP Team Hub..."
$MVN -f modules/product/pom.xml javafx:run
