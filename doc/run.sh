#!/bin/bash
# Install to: /opt/dse/demo/run.sh

BASE_DIR="/opt/dse/demo"

# X11 Display on localhost
export DISPLAY=:0

# JavaFX
export PATH_TO_FX=/usr/share/openjfx/lib

# Java options
JAVA_OPTS="-XX:+ExitOnOutOfMemoryError"

exec 2>&1
exec java $JAVA_OPTS -jar "${BASE_DIR}/demo-all.jar"
