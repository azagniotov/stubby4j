#!/usr/bin/env sh

VERSION=$( sed -nr 's/^stubbyProjectVersion=([0-9.]+)-?.*$/\1/p' ../../gradle.properties )
JDK=$( sed -nr 's/FROM gradle:[0-9.]+-(jdk[0-9]+).*/\1/p' Dockerfile )

if [ $# -ne 1 ]; then
    echo "Usage: ${0} DOCKER_HUB_USERNAME"
    exit 1
fi

docker buildx create --name multiarch-builder
docker buildx use multiarch-builder
docker buildx build --platform linux/arm64,linux/amd64 --tag ${1}/stubby4j:${VERSION}-${JDK} --push .
docker buildx rm multiarch-builder
