FROM	dockerfile/java:oracle-java8
MAINTAINER varkockova.a@gmail.com

RUN apt-get update

RUN wget https://repo1.maven.org/maven2/by/stub/stubby4j/3.2.3/stubby4j-3.2.3.jar

EXPOSE 8882 8889

ENTRYPOINT ["java", "-jar", "stubby4j-3.2.3.jar"]
