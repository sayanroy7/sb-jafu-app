#!/usr/bin/env bash

ARTIFACT=sb-jafu-app
MAINCLASS=sb.jafu.app.JafuApplication
VERSION=0.0.1-SNAPSHOT

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

rm -rf target
mkdir -p target/native-image

echo "Packaging $ARTIFACT with Maven"
./mvnw -DskipTests package > target/native-image/output.txt

JAR="$ARTIFACT-$VERSION.jar"
rm -f $ARTIFACT
echo "Unpacking $JAR"
cd target/native-image
jar -xvf ../$JAR >/dev/null 2>&1
cp -R META-INF BOOT-INF/classes

LIBPATH=`find BOOT-INF/lib | tr '\n' ':'`
CP=BOOT-INF/classes:$LIBPATH

GRAALVM_VERSION=`native-image --version`
echo "Compiling $ARTIFACT with $GRAALVM_VERSION"
{ time native-image \
  --verbose \
  --no-server \
  --no-fallback \
  -H:EnableURLProtocols=http \
  --initialize-at-build-time \
  --initialize-at-run-time=org.springframework.boot.autoconfigure,com.mongodb,org.springframework.data,com.github.jnr,com.github.jnr,org.xerial.snappy,com.github.luben,io.nettyorg.mongodb,org.springframework.core.io.VfsUtils,org.apache.tomcat.jni.SSL \
  -H:ReflectionConfigurationFiles=../../tomcat-reflection.json,../../reflection-config.json -H:ResourceConfigurationFiles=../../tomcat-resource.json \
  -H:Name=$ARTIFACT \
  -H:+TraceClassInitialization \
  -H:+ReportExceptionStackTraces \
  -H:EnableURLProtocols=https \
  --allow-incomplete-classpath \
  --report-unsupported-elements-at-runtime \
  -cp $CP $MAINCLASS >> output.txt ; } 2>> output.txt

if [[ -f $ARTIFACT ]]
then
  printf "${GREEN}SUCCESS${NC}\n"
  mv ./$ARTIFACT ..
  exit 0
else
  printf "${RED}FAILURE${NC}: an error occurred when compiling the native-image.\n"
  exit 1
fi
