# A few useful Docker commands to build an image and run the stubby4j container.
#
# Build (run with '--no-cache' to ensure that Git repo new tags will be pulled down, as Docker caches RUNs):
# '$ docker build --build-arg REVISION=v7.2.0 --rm --no-cache -t stubby4j:latest .' or
# '$ docker build --rm --no-cache -t stubby4j:latest .' to build from master
#
# Run:
# https://hub.docker.com/r/azagniotov/stubby4j ("How to use this image")

########################################################################################
# Stage 1 : build the app
########################################################################################
FROM gradle:7.2.0-jdk8-openj9@sha256:d4128edf5ac579f93e3149a9f89be3af779de1d3d85a9f25f25cb3d4b432b69a AS BUILD_JAR_STAGE

ARG REVISION=master
ENV GRADLE_USER_HOME=/home/gradle
WORKDIR $GRADLE_USER_HOME

# Build from the latest tag
RUN git clone https://github.com/azagniotov/stubby4j.git && \
      cd stubby4j && \
      git fetch -f --tags && \
      git checkout $REVISION && \
      gradle -Plog4j clean jar

########################################################################################
# Stage 2 : create the Docker final image
########################################################################################
# https://hub.docker.com/r/azul/zulu-openjdk-alpine
FROM azul/zulu-openjdk-alpine:8u282-8.52.0.23-jre-headless@sha256:91fe8ae651d5c778312e946e89f934240c2a80f55b7ca894d76daef8f5281375

ARG USER_ID=1001
ARG GROUP_ID=1001

MAINTAINER Alexander Zagniotov <azagniotov@gmail.com>

# Why --location=0.0.0.0 ??? Read: https://stackoverflow.com/a/59182290
ENV LOCATION=0.0.0.0 \
    STUBS_PORT=8882 \
    STUBS_TLS_PORT=7443 \
    ADMIN_PORT=8889 \
    WITH_ARGS="" \
    YAML_CONFIG="main.yaml" \
    STUBBY4J_USER_HOME=/home/stubby4j

# Users & permissions, docs: https://wiki.alpinelinux.org/wiki/Setting_up_a_new_user
RUN addgroup --system --gid $GROUP_ID stubby4j && \
    adduser \
        --system \
        --disabled-password \
        --gecos '' \
        --uid $USER_ID stubby4j \
        --shell /bin/bash \
        --home "$STUBBY4J_USER_HOME" && \
    chown --recursive stubby4j:stubby4j "$STUBBY4J_USER_HOME"

WORKDIR "$STUBBY4J_USER_HOME"

COPY --from=BUILD_JAR_STAGE /home/gradle/stubby4j/build/libs/stubby4j*SNAPSHOT.jar ./stubby4j.jar
COPY --from=BUILD_JAR_STAGE /home/gradle/stubby4j/docker/log4j2-for-docker.xml ./log4j2.xml
RUN chown $USER_ID:$GROUP_ID stubby4j.jar && \
    chown $USER_ID:$GROUP_ID log4j2.xml && \
    java -version && \
    ls -al

# Set the UID and GID of 'stubby4j' for the ENTRYPOINT instructions,
# because root inside the container == root outside the container,
# since there’s a single kernel and a single, shared pool of UIDs and GIDs.
USER $USER_ID:$GROUP_ID

# Mark the 'data' directory as volume
VOLUME "$STUBBY4J_USER_HOME/data"

# Expose the three stubby4j ports and run the JAR
EXPOSE $ADMIN_PORT $STUBS_PORT $STUBS_TLS_PORT

# https://www.docker.com/blog/apache-log4j-2-cve-2021-44228/
ENTRYPOINT java \
        -DoverrideDisabledAlgorithms=true \
        -Dlog4j.formatMsgNoLookups=true \
        -Dlog4j2.configurationFile=log4j2.xml \
        -jar stubby4j.jar \
            --location ${LOCATION} \
            --admin ${ADMIN_PORT} \
            --stubs ${STUBS_PORT} \
            --tls ${STUBS_TLS_PORT} \
            --data data/${YAML_CONFIG} \
            --mute \
            ${WITH_ARGS}
