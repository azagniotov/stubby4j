# A few useful Docker commands to build an image and run the stubby4j container.
#
# Build (run with '--no-cache' to ensure that Git repo new tags will be pulled down, as Docker caches RUNs):
# 'docker build --no-cache -t stubby4j:latest .'
#
# Run:
# 'docker run -p 8882:8882 -p 8889:8889 -p 7443:7443 stubby4j'

########################################################################################
# Stage 1 : build the app
########################################################################################
FROM gradle:6.7.1-jdk8-openj9 AS BUILD_JAR_STAGE

ENV GRADLE_USER_HOME=/home/gradle
WORKDIR $GRADLE_USER_HOME

# Build from the latest tag
RUN git clone https://github.com/azagniotov/stubby4j.git && \
      cd stubby4j && \
      git fetch -f --tags && \
      git checkout v7.1.1 && \
      gradle clean jar

########################################################################################
# Stage 2 : create the Docker final image
########################################################################################
FROM adoptopenjdk/openjdk8-openj9:alpine

MAINTAINER Alexander Zagniotov <azagniotov@gmail.com>

# Why --location=0.0.0.0 ??? Read: https://stackoverflow.com/a/59182290
ENV LOCATION=0.0.0.0
ENV STUBS_PORT=8882
ENV STUBS_TLS_PORT=7443
ENV ADMIN_PORT=8889
ENV WITH_DEBUG=""
ENV WITH_WATCH=""
ENV YAML_CONFIG="main.yaml"

ENV STUBBY4J_USER_HOME=/home/stubby4j

# Users & permissions, docs: https://wiki.alpinelinux.org/wiki/Setting_up_a_new_user
RUN addgroup --system --gid 1007 stubby4j && \
      adduser --system --uid 1007 stubby4j --shell /bin/bash --home "$STUBBY4J_USER_HOME" && \
      chown --recursive stubby4j:stubby4j "$STUBBY4J_USER_HOME"

# Mark the 'data' directory as volume
VOLUME "$STUBBY4J_USER_HOME/data"
WORKDIR "$STUBBY4J_USER_HOME"

COPY --from=BUILD_JAR_STAGE /home/gradle/stubby4j/build/libs/stubby4j*SNAPSHOT.jar ./stubby4j.jar
RUN chown stubby4j:stubby4j stubby4j.jar && ls -al

# Set the UID and GID to stubby4j for the ENTRYPOINT instructions
USER stubby4j:stubby4j

# Expose the three stubby4j ports and run the JAR
EXPOSE $ADMIN_PORT $STUBS_PORT $STUBS_TLS_PORT
ENTRYPOINT java -jar stubby4j.jar \
      --location ${LOCATION} \
      --admin ${ADMIN_PORT} \
      --stubs ${STUBS_PORT} \
      --tls ${STUBS_TLS_PORT} \
      --data data/${YAML_CONFIG} \
      ${WITH_WATCH} \
      ${WITH_DEBUG}
