#!/usr/bin/env sh

 SOURCE="${BASH_SOURCE[0]}"
 echo "[INFO] Running "$SOURCE
# Will work with spaces and other weird characters
# Quote everything so that no strange characters can cause havoc
 DIR="$( cd "$( dirname "$0" )" && pwd )"
 cp $DIR/target/stubby4j-*.jar $DIR/artifacts/
 echo "[INFO] Copied $DIR/target/stubby4j-*.jar to $DIR/artifacts/"
