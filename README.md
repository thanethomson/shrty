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

### Create your first user
To be able to log in, and to see the API documentation, you will need to create
a user for yourself. The quickest way is to use [cURL](http://curl.haxx.se/)
from your system terminal:

```bash
$ curl -H "Content-Type: application/json" -X POST -d '{"email": "your.email@address.com",'\
'"firstName": "Firstname", "lastName": "Surname",'\
'"password": "YoUr_PaSsw0rd"}' http://localhost:9000/api/user 
```

If your request was successful, you should receive something resembling the
following JSON response:

```javascript
{
  "id": 3,
  "firstName": "Firstname",
  "lastName": "Surname",
  "email": "your.email@address.com",
  "created": "2015-09-09T16:27:25+0200"
}
```

And you should be able to log in with your submitted e-mail address and password.

### Security
As you can probably see, the user creation approach outlined here is definitely
not secure. One of the future improvements planned for the project is a better
way of signing users up (perhaps Active Directory or OAuth integration).


## API Documentation
Once your **Shrty** instance is up and running, navigate to `/api` in your web
browser and you will have access to the latest documentation for the API. This
documentation is generated from a [RAML](http://raml.org/) source file in
`conf/api.raml`. Make sure you're logged in though before attempting to access
this URL.


## License
The MIT License (MIT)

Copyright (c) 2015 Thane Thomson

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.