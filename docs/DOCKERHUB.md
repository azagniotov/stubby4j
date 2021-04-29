# Official `stubby4j` Docker Images

[![DockerHub][docker-hub-badge]][docker-hub-link]
[![GitHubStars][stars-badge]][stars-link]
[![GitHubForks][forks-badge]][forks-link]
[![Stackoverflow stubby4j][stackoverflow-badge]][stackoverflow-link]
[![Maven Central][maven-badge]][maven-link]


[![stubb4j][logo-badge]][logo-link]




# Quick reference
* __Source repository__: https://github.com/azagniotov/stubby4j
* __Maintained by__: [Alexander Zagniotov (of the stubby4j project)](https://github.com/azagniotov/stubby4j)
* __Where to get help__: https://github.com/azagniotov/stubby4j/issues or [StackOverflow](http://stackoverflow.com/questions/tagged/stubby4j)
* __Changelog__: https://github.com/azagniotov/stubby4j/blob/master/docs/CHANGELOG.md


# Supported tags and respective `Dockerfile` links

The following `stubby4j` Docker images are available ([Alpine-Native Zulu OpenJDK](https://hub.docker.com/r/azul/zulu-openjdk-alpine) images used as base):
* Built from tagged `stubby4j` `v7.x.x` versions (image tag naming convention is `7.x.x-jreXX`)
* Built from default (i.e.: `master`)  branch (image tag naming convention is `latest-jreXX`)

See https://github.com/azagniotov/stubby4j/blob/master/docs/CHANGELOG.md for more information

#### stubby4j versions on Alpine-Native Zulu OpenJDK JRE 8
* [`7.3.3-jre8`](https://github.com/azagniotov/stubby4j/blob/v7.3.3/docker/jdk8/Dockerfile)
* [`latest-jre8`](https://github.com/azagniotov/stubby4j/blob/master/docker/jdk8/Dockerfile)

#### stubby4j versions on Alpine-Native Zulu OpenJDK JRE 11
* [`7.3.3-jre11`](https://github.com/azagniotov/stubby4j/blob/v7.3.3/docker/jdk11/Dockerfile)
* [`latest-jre11`](https://github.com/azagniotov/stubby4j/blob/master/docker/jdk11/Dockerfile)

#### stubby4j versions on Alpine-Native Zulu OpenJDK JRE 15
* [`7.3.3-jre15`](https://github.com/azagniotov/stubby4j/blob/v7.3.3/docker/jdk15/Dockerfile)
* [`latest-jre15`](https://github.com/azagniotov/stubby4j/blob/master/docker/jdk15/Dockerfile)


# What is `stubby4j`? <img src="https://cdn.rawgit.com/azagniotov/stubby4j/master/assets/stubby-logo-duke-hiding.svg" width="65px" height="65px" />

A highly flexible and configurable tool for testing interactions of service-oriented (SoA) or/and micro-services architectures (REST, SOAP, WSDL etc.) over HTTP(s) protocol, in both containerized (i.e.: Docker) and non-containerized environments.

### Some of the `stubby4j` key features
```
* Stub out external services in a Docker based micro-service architecture
* Avoid negative productivity impact when an API you depend on doesn't exist or isn't complete
* Request proxying - ability to configure a proxy/intercept where requests are proxied to another service
* Simulate edge cases and/or failure modes that the real API won't reliably produce
* Fault injection, where after X good responses on the same URI you get a bad one
```
... and much more! Please refer to https://github.com/azagniotov/stubby4j for more information about the features and capabilities.

# How to use this image

## Environment variables

`stubby4j` images use environment variables for configuration. Pre `v7.2.0` (excl.), the following are the available environment variables:

|Available variables     |Default value        |Description                                         |
|------------------------|------------------------|----------------------------------------------------|
|`YAML_CONFIG`    | `main.yaml`          |YAML config with stub definitions filename|
|`STUBS_PORT`    | `8882`|Port for stub portal|
|`ADMIN_PORT` |`8889`|Port for admin portal |
|`STUBS_TLS_PORT`   | `7443` |Port for stub portal over SSL|
|`WITH_DEBUG`     | no default |Dumps raw HTTP request to the console |
|`WITH_WATCH`     | no default |Periodically scans for changes & reloads YAML configs |

As of `v7.2.0` (incl.), there is an additional environment variable available:

|Available variables     |Default value        |Description                                         |
|------------------------|------------------------|----------------------------------------------------|
|`WITH_STUB_CACHE_DISABLED`| no default |Disables matched stubs in-memory caching  |

See https://github.com/azagniotov/stubby4j#command-line-switches for more information.

## Starting a `stubby4j` instance

Please note that in all `stubby4j` Docker images, the `UID`/`GID` `1007` (user `stubby4j` & group `stubby4j`) is set before the `ENTRYPOINT` instruction to avoid running the service as root.

### Basic command

Starting a `stubby4j` instance is simple using the most basic following command:
```
$ docker run --rm \
    --env YAML_CONFIG=stubs.yaml \
    --volume <HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>:/home/stubby4j/data \
    -p 8882:8882 -p 8889:8889 -p 7443:7443 \
    azagniotov/stubby4j:<TAG>
```
... where:
* `<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>` is the host machine directory with the `stubby4j` YAML config file (see the `YAML_CONFIG` env var below) that you want to map to the container volume `/home/stubby4j/data`
* Passing `stubs.yaml` to `YAML_CONFIG` env var as the name of the main YAML config
* `-p` publishes/exposes default container's ports `8882`, `8889` & `7443` for stubs, admin & stubs on SSL portals respectively to the host.
* `<TAG>` is the tag specifying the `stubby4j` version you want. See the list above for relevant tags

e.g.:
```
$ docker run --rm \
    --env YAML_CONFIG=stubs.yaml \
    --volume /Users/zaggy/docker-playground/yaml:/home/stubby4j/data \
    -p 8882:8882 -p 8889:8889 -p 7443:7443 \
    azagniotov/stubby4j:7.3.3-jre8
```

### Full command

The following command uses all environment variables when starting a `stubby4j` instance:

```
docker run --rm \
    --env YAML_CONFIG=stubs.yaml \
    --env STUBS_PORT=9991 \
    --env ADMIN_PORT=8889 \
    --env STUBS_TLS_PORT=8443 \
    --env WITH_STUB_CACHE_DISABLED=--disable_stub_caching \
    --env WITH_DEBUG=--debug \
    --env WITH_WATCH=--watch \
    --volume /Users/zaggy/docker-playground/yaml:/home/stubby4j/data \
    -p 9991:9991 -p 8889:8889 -p 8443:8443 \
    azagniotov/stubby4j:7.3.3-jre8
```

... where the command:
* Passes `stubs.yaml` to `YAML_CONFIG` env var as the name of the main YAML config under `/Users/zaggy/docker-playground/yaml`
* Passes `9991` to `STUBS_PORT` env var as the port for stubs portal
* Passes `8889` to `ADMIN_PORT` env var as the port for admin portal
* Passes `8443` to `STUBS_TLS_PORT` env var as the port for stubs portal on SSL
* Passes `--disable_stub_caching ` to `WITH_STUB_CACHE_DISABLED` env var to disable stubs in-memory caching when stubs are successfully matched to the incoming HTTP requests. If the `WITH_STUB_CACHE_DISABLED` is not set, then the in-memory cache is enabled by default.
* Passes `--debug ` to `WITH_DEBUG` env var to make `stubby4j` to dump raw incoming HTTP requests to the console. If the `WITH_DEBUG` is not set, then the dumping incoming HTTP requests is disabled by default.
* Passes `--watch ` to `WITH_WATCH` env var to make `stubby4j` to periodically scan for changes in last modification date of the YAML configs and referenced external files (if any). The watch scans every 100ms. If last modification date changed since the last scan period, the stub configuration is reloaded. If the `WITH_WATCH` is not set, then the periodic scan is disabled by default.
* `-p` publishes/exposes set container's ports `9991`, `8889` & `8443` for stubs, admin & stubs on SSL portals respectively to the host.
* `7.3.3-jre8` is the tag specifying the `stubby4j` version. See the list above for relevant tags

See https://github.com/azagniotov/stubby4j#command-line-switches for more information

## Running in Docker Compose

You can add `stubby4j` image to your stack using Docker Compose:

```
# This compose file adds stubby4j https://hub.docker.com/r/azagniotov/stubby4j to your stack
# See "Environment variables" section at https://hub.docker.com/r/azagniotov/stubby4j
version: '3.8'
services:
  stubby4j:
    image: azagniotov/stubby4j:latest-jre8 # you can also use other tags: latest-jre11, latest-jre15
    volumes:
      - "<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>:/home/stubby4j/data"
    container_name: stubby4j
    ports:
      - 8882:8882
      - 8889:8889
      - 7443:7443
    environment:
      YAML_CONFIG: main.yaml
      STUBS_PORT: 8882
      ADMIN_PORT: 8889
      STUBS_TLS_PORT: 7443
      WITH_DEBUG: --debug
      WITH_WATCH: --watch
```
... where the `<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>` is the host machine directory with the `stubby4j` YAML config file (see the `YAML_CONFIG` env var) that you want to map to the container volume `/home/stubby4j/data`


## Container application logs

Currently, only in the Docker images tagged as `latest-jreXX` and `7.3.3-jreXX`, the `stubby4j` service emits file-based logs (i.e.: generated by `log4j2` library) as well as `STDOUT` output stream logs. In images tagged as <= `7.3.2-jreXX`, only `STDOUT` output stream logs are available.

The file-based logs are persisted in the container volume `/home/stubby4j/data` under `logs` directory,  i.e.:`/home/stubby4j/data/logs`

When starting a `stubby4j` instance using the `docker run ...` command, e.g.:
```
$ docker run --rm \
    --env YAML_CONFIG=stubs.yaml \
    --volume <HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>:/home/stubby4j/data \
    -p 8882:8882 -p 8889:8889 -p 7443:7443 \
    ...
    ...
    ...
```

... where the `<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>` is the host machine directory with the `stubby4j` YAML config that will be mapped to the aforementioned container volume `/home/stubby4j/data`. The `logs` directory will appears under the aforementioned host machine directory, i.e.: `<HOST_MACHINE_DIR_WITH_YAML_CONFIG_TO_MAP_VOLUME_TO>/logs`


# Managing stubs via REST API

Upon running the `stubby4j` container as per aforementioned examples, the Admin portal runs on `localhost`:`<port>` (e.g.: `localhost`:`8889`) or wherever you described through `stubby4j` image environment variables. The admin portal exposes a set of REST APIs that enable management of the loaded in-memory stubs, which were loaded from the YAML config provided to `stubby4j` during start-up.

See https://github.com/azagniotov/stubby4j/blob/master/docs/ADMIN_PORTAL.md for more information

# License
[View license information](https://github.com/azagniotov/stubby4j/blob/master/LICENSE) for the software contained in this image.

As with all Docker images, these likely also contain other software which may be under other licenses (such as Bash, etc from the base distribution, along with any direct or indirect dependencies of the primary software being contained).

As for any pre-built image usage, it is the image user's responsibility to ensure that any use of this image complies with any relevant licenses for all software contained within.

<!-- references -->

[circleci-badge]: https://circleci.com/gh/azagniotov/stubby4j.svg?style=shield
[circleci-link]: https://circleci.com/gh/azagniotov/stubby4j

[maven-badge]: https://img.shields.io/maven-central/v/io.github.azagniotov/stubby4j.svg?style=flat&label=maven-central
[maven-link]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.azagniotov%22%20AND%20a%3A%22stubby4j%22

[stackoverflow-badge]: https://img.shields.io/badge/stackoverflow-stubby4j-brightgreen.svg?style=flat
[stackoverflow-link]: http://stackoverflow.com/questions/tagged/stubby4j

[chat-badge]: https://badges.gitter.im/Join%20Chat.svg
[chat-link]: https://gitter.im/stubby4j/Lobby

[license-badge]: https://img.shields.io/badge/license-MIT-blue.svg?style=flat
[license-link]: http://badges.mit-license.org

[docker-hub-badge]: https://img.shields.io/docker/pulls/azagniotov/stubby4j.svg?style=flat
[docker-hub-link]: https://hub.docker.com/r/azagniotov/stubby4j

[stars-badge]: https://img.shields.io/github/stars/azagniotov/stubby4j.svg?color=success
[stars-link]: https://github.com/azagniotov/stubby4j

[logo-badge]: https://cdn.rawgit.com/azagniotov/stubby4j/master/assets/stubby-logo-duke-hiding.svg
[logo-link]: https://github.com/azagniotov/stubby4j

[forks-badge]: https://img.shields.io/github/forks/azagniotov/stubby4j.svg
[forks-link]: https://github.com/azagniotov/stubby4j
