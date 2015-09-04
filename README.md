# Shrty

## Overview
This is a simple web-based service (with API) that provides URL shortening functionality. Configured properly, it could
potentially be scaled up to serve large volumes of requests.

## System Requirements
The following software is required to get an instance of **Shrty** up and running:

* Java >= 8
* PostgreSQL >= 9.0

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

### Run the app!
```bash
$ cd shrty/
$ ./activator run
```

It may take a while to pull in all of the dependencies for the application, but once
that's done you should have a server up and running (bound to `localhost` port 9000).


## License
This software is licensed under the *MIT License*. See `LICENSE.md` for details.