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
FROM gradle:6.8.3-jdk15@sha256:642395fe8f800d292d01073ecdb8ffd0d4995305b5856fda6a8cf52514877183 AS BUILD_JAR_STAGE

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
FROM azul/zulu-openjdk-alpine:15.0.0-15.27.17-jre@sha256:d3635678a62452828e0ee7f7ef2c5a61e5833a06a10f04a7fa5f0081541cb227

MAINTAINER Alexander Zagniotov <azagniotov@gmail.com>

# Why --location=0.0.0.0 ??? Read: https://stackoverflow.com/a/59182290
ENV LOCATION=0.0.0.0
ENV STUBS_PORT=8882
ENV STUBS_TLS_PORT=7443
ENV ADMIN_PORT=8889
ENV WITH_DEBUG=""
ENV WITH_WATCH=""
ENV WITH_STUB_CACHE_DISABLED=""
ENV YAML_CONFIG="main.yaml"

ENV STUBBY4J_USER_HOME=/home/stubby4j

# Users & permissions, docs: https://wiki.alpinelinux.org/wiki/Setting_up_a_new_user
RUN addgroup --system --gid 1000 stubby4j && \
      adduser --system --uid 1000 stubby4j --shell /bin/bash --home "$STUBBY4J_USER_HOME" && \
      chown --recursive stubby4j:stubby4j "$STUBBY4J_USER_HOME"

WORKDIR "$STUBBY4J_USER_HOME"

COPY --from=BUILD_JAR_STAGE /home/gradle/stubby4j/build/libs/stubby4j*SNAPSHOT.jar ./stubby4j.jar
COPY --from=BUILD_JAR_STAGE /home/gradle/stubby4j/docker/log4j2-for-docker.xml ./log4j2.xml
RUN chown stubby4j:stubby4j stubby4j.jar && \
    chown stubby4j:stubby4j log4j2.xml && \
    java -version && \
    ls -al

# Set the UID and GID to stubby4j for the ENTRYPOINT instructions
USER stubby4j:stubby4j

# Mark the 'data' directory as volume
VOLUME "$STUBBY4J_USER_HOME/data"

# Expose the three stubby4j ports and run the JAR
EXPOSE $ADMIN_PORT $STUBS_PORT $STUBS_TLS_PORT
ENTRYPOINT java -Dlog4j2.configurationFile=log4j2.xml -jar stubby4j.jar \
      --location ${LOCATION} \
      --admin ${ADMIN_PORT} \
      --stubs ${STUBS_PORT} \
      --tls ${STUBS_TLS_PORT} \
      --data data/${YAML_CONFIG} \
      --mute \
      ${WITH_STUB_CACHE_DISABLED} \
      ${WITH_DEBUG} \
      ${WITH_WATCH}
