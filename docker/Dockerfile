# Provides the Shrty URL shortener running on top of Ubuntu 14.04.2 with
# Oracle JDK 8.
FROM ubuntu:14.04.2
MAINTAINER Thane Thomson <connect@thanethomson.com>

# Update and upgrade Ubuntu packages
RUN apt-get update && apt-get -y upgrade

# Set up the JDK
COPY files/jdk-8u45-linux-x64.tar.gz /tmp/
RUN cd /tmp && tar -xf /tmp/jdk-8u45-linux-x64.tar.gz && mkdir /usr/lib/jvm && mv /tmp/jdk1.8.0_45 /usr/lib/jvm/oraclejdk8
ENV LANG=C.UTF-8
ENV J2SDKDIR=/usr/lib/jvm/oraclejdk8
ENV J2REDIR=/usr/lib/jvm/oraclejdk8/jre
ENV JAVA_HOME=/usr/lib/jvm/oraclejdk8
ENV DERBY_HOME=/usr/lib/jvm/oraclejdk8/db
ENV PATH=$PATH:/usr/lib/jvm/oraclejdk8/bin:/usr/lib/jvm/oraclejdk8/db/bin:/usr/lib/jvm/oraclejdk8/jre/bin

# Make sure we have git
RUN apt-get -y install git

# Get the Shrty repo on the master branch
RUN cd /opt && git clone https://github.com/thanethomson/shrty.git && mkdir /opt/shrty/logs
# Copy the application's production config across
COPY files/application_production.conf /opt/shrty/conf/
# Build it so it can pull in the dependencies prior to starting the container
RUN cd /opt/shrty && ./activator compile
# By default the application will expose itself on port 9000
EXPOSE 9000
# Keep our logs
VOLUME /opt/shrty/logs
# Make sure we have the right config file configured for production
ENV config.resource=application_production.conf

# Our primary command
CMD ["/opt/shrty/activator", "run"]

