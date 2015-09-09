# Docker Config for Shrty

## Overview
Shrty can be easily configured to be run in a Docker container. This particular
configuration allows for one to containerise the version of Shrty currently on
the `master` branch in the GitHub repo.

## JDK
This containerised version of Shrty requires a specific version of Oracle's JDK
version 8 (8u45, to be exact). This is not included in this repo due to
licensing restrictions, and you will need to download that specific version of
the JDK to be able to build this image. Try to find the
`jdk-8u45-linux-x64.tar.gz` file from Oracle's JDK download page (version 8u45
of the JDK for Linux), and copy it into the `files/` subfolder in this
directory.

## Building the Image
To build your image, after making sure you have the relevant JDK version in
your `files` folder, build your image as follows (as `root`):

```bash
docker build -t <yourusername>/shrty .
```

## Environment Variables
The following environment variables are important to configure when running
your Shrty container:

* `SHRTY_SECRET`: See [https://www.playframework.com/documentation/2.4.x/ApplicationSecret](https://www.playframework.com/documentation/2.4.x/ApplicationSecret)
* `SHRTY_DB_URL`: The URL to your PostgreSQL database instance (e.g.
`postgres://postgreshost/shrty`).
* `SHRTY_DB_PASSWORD`: The password for the `shrty` role to access your
PostgreSQL instance.
* `SHRTY_REDIS_HOST`: The host address/name for your Redis instance.
* `SHRTY_REDIS_PORT`: The port for your Redis instance.

## Running Your Container
Once you've decided on your various environment variables and you have your
PostgreSQL and Redis instances up and running, you can run your Shrty container
with the following command (as `root`):

```bash
docker run \                       # basic run command
       -d  \                       # daemonize
       -name shrty \               # give our container a name
       --link postgres:postgres \  # link to your Postgres container, if applicable
       --link redis:redis       \  # link to your Redis container, if applicable
       -e SHRTY_SECRET="<your_application_secret>" \
       -e SHRTY_DB_URL="postgres://postgres/shrty" \
       -e SHRTY_DB_PASSWORD="<your_postgres_password>" \
       -e SHRTY_REDIS_HOST="redis" \
       -e SHRTY_REDIS_PORT=6379 \
       <yourusername>/shrty        # the image tag you specified earlier
```

Of course, the above assumes that you have a PostgreSQL 9.4 container running
with name `postgres`, as well as a Redis 3+ container running with name
`redis`.