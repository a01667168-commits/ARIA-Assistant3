#!/usr/bin/env sh
##############################################################################
# Gradle start up script (estándar). Requiere gradle-wrapper.jar en
# gradle/wrapper/ — ver INSTRUCCIONES.md si falta.
##############################################################################
DEFAULT_JVM_OPTS=""
APP_HOME=$(cd "$(dirname "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec java $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
