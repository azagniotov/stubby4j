# A few useful Docker commands to build an image and run the stubby4j container:
#
# Build:
# docker build -t stubby4j:latest .
#
# Run:
# docker run -p 8882:8882 -p 8889:8889 stubby4j

# Stage 1 : build the app
FROM gradle:6.7.1-jdk8-openj9 AS build-jar-stage
MAINTAINER azagniotov@gmail.com
ENV GRADLE_USER_HOME=/home/gradle

# Clone stubby4j repo
WORKDIR $GRADLE_USER_HOME
RUN git clone https://github.com/azagniotov/stubby4j.git

# Build from the latest tag
WORKDIR stubby4j
RUN git fetch --tags
RUN LATEST_RELEASE_TAG="$(git tag --sort=committerdate | tail -1)"; git checkout "$LATEST_RELEASE_TAG"
RUN gradle clean jar

# Stage 2 : create the Docker final image
FROM adoptopenjdk/openjdk8-openj9:alpine
ENV STUBBY4J=stubby4j
ENV STUBBY4J_USER_HOME=/home/$STUBBY4J

RUN addgroup -S $STUBBY4J && adduser -S $STUBBY4J -G $STUBBY4J
USER $STUBBY4J:$STUBBY4J
COPY --from=build-jar-stage /home/gradle/$STUBBY4J/build/libs/$STUBBY4J*SNAPSHOT.jar $STUBBY4J_USER_HOME/$STUBBY4J.jar

# Expose ports and run the JAR
WORKDIR $STUBBY4J_USER_HOME
EXPOSE 8882 8889

# Why --location=0.0.0.0 ??? Read: https://stackoverflow.com/a/59182290
ENTRYPOINT ["java", "-jar", "stubby4j.jar", "--location", "0.0.0.0"]
