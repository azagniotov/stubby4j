FROM	dockerfile/java:oracle-java8
MAINTAINER varkockova.a@gmail.com

RUN apt-get update

RUN wget https://repo1.maven.org/maven2/io/github/azagniotov/stubby4j/4.0.2/stubby4j-4.0.2.jar

EXPOSE 8882 8889

ENTRYPOINT ["java", "-jar", "stubby4j-4.0.2jar"]
