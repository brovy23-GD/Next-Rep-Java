#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p build/classes build/test-classes
javac --release 21 -d build/classes $(find src/main/java -name '*.java')
case "${1:-}" in
  test)
    javac --release 21 -cp build/classes -d build/test-classes $(find src/test/java -name '*.java')
    java -cp build/classes:build/test-classes com.rovy.nextrep.ProjectTests
    ;;
  *) java -cp build/classes com.rovy.nextrep.Main "$@" ;;
esac
