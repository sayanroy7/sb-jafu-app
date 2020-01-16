#!/bin/bash -x

# common
JAR_FILE=target/sb-*-SNAPSHOT.jar
EP_CFG="-Dmanagement.endpoints.web.exposure.include=* -Dmanagement.endpoint.shutdown.enabled=true"
DEBUG_PORT=18080
ARG_DEBUG="-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=${DEBUG_PORT}"
JMX_PORT=28080
ARG_JMX="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=${JMX_PORT} -Dcom.sun.management.jmxremote.rmi.port=${JMX_PORT} -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

java \
    "${EP_CFG}" "${ARG_DEBUG}" "${ARG_JMX}" \
    -jar -Xms1g -Xmx2g -XX:+UseParallelGC ${JAR_FILE}