#!/bin/bash

echo "pulling 7.2.0-zulu-openjdk-alpine-15.0.0-15.27.17-jre"
docker pull azagniotov/stubby4j:7.2.0-zulu-openjdk-alpine-15.0.0-15.27.17-jre

echo "pulling 7.2.0-zulu-openjdk-alpine-11.0.7-jre"
docker pull azagniotov/stubby4j:7.2.0-zulu-openjdk-alpine-11.0.7-jre

echo "pulling 7.2.0-zulu-openjdk-alpine-8u252-jre"
docker pull azagniotov/stubby4j:7.2.0-zulu-openjdk-alpine-8u252-jre

echo "pulling 7.1.3-zulu-openjdk-alpine-8u252-jre"
docker pull azagniotov/stubby4j:7.1.3-zulu-openjdk-alpine-8u252-jre

echo "listing images"
docker images

echo "deleting images"
docker rmi -f $(docker images -a -q)

