#!/usr/bin/env bash

rm -rf /tmp/jdk11
(cd /tmp; curl https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_linux-x64_bin.tar.gz | gunzip -c | tar xf -)
mv /tmp/jdk-11.0.1 /tmp/jdk11
du -sh /tmp
(JAVA_HOME=/tmp/jdk11 PATH=/tmp/jdk11/bin:$PATH java -version)