## Building
Run `mvn clean package` command to:
* compile the Java sources
* run unit and integration tests
* package four stubby4j JARs: fat, skinny (no deps), sources and javadoc under `target` directory

Run the `mvn site` command to generate `site` directory under `target` with the following reports:
* PMD
* FindBugs
* CPD
* Javadocs

The commands can be also run in one go: `mvn clean package site`