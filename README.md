# Shrty

## Overview
This is a simple web-based service (with API) that provides URL shortening functionality.
Configured properly, it could potentially be scaled up to serve large volumes of requests.
**Shrty** offers the following features:

* Web-based administration interface for short URL management.
* RESTful API for user and URL management.
* Cache system integration ([EHCache](http://ehcache.org/) and [Redis](http://redis.io/))
for high-speed, distributed operation.

## System Requirements
The following software is required to get an instance of **Shrty** up and running:

* Java >= 8
* PostgreSQL >= 9.0
* Redis >= 3 (if you don't want to use EHCache)

## Quickstart
To get an instance of **Shrty** up and running on your machine quickly, do the following.

### Set up PostgreSQL database
Once you have PostgreSQL installed on your machine, create a database and role for **Shrty**.

```bash
$ createuser --login --pwprompt --echo shrty
... (enter password for shrty: shrty)

$ createdb --owner=shrty --echo shrty
```

You could set up your own credentials, but then you will need to change your database
configuration in `conf/application_dev.conf` (see the `db.default.url` setting).

### Get the code
In an appropriate working directory on your machine, check out the repository.

```bash
$ git clone https://github.com/thanethomson/shrty.git
```

### Set up your cache system config (optional)
At the moment, the application is configured to access a Redis server on your
`localhost` on port 6379. If you want to rather use Play's built-in EHCache
caching service, open up your `conf/application_dev.conf` and
`conf/application_test.conf` files and set the following:

```
shrty {
  cache {
    # change the "redis" value to "ehcache" if you want EHCache
    system = "ehcache"
  }
}
```

### Run the app!
```bash
$ cd shrty/
$ ./activator run
```

It may take a while to pull in all of the dependencies for the application, but once
that's done you should have a server up and running (bound to `localhost` port 9000).

## API Documentation
Once your **Shrty** instance is up and running, navigate to `/api` in your web
browser and you will have access to the latest documentation for the API. This
documentation is generated from a [RAML](http://raml.org/) source file in
`conf/api.raml`.

## License
This software is licensed under the *MIT License*. See `LICENSE.md` for details.