# --- Base Shrty schema

# --- !Ups

create table users (
  id serial,
  firstName varchar(200),
  lastName varchar(200),
  email varchar(300),
  passwordHash varchar(100),
  created timestamp with time zone,
  primary key(id)
);

create table short_urls (
  id bigserial,
  shortCode varchar(50),
  url text,
  hitCount bigint,
  created timestamp with time zone,
  createdBy integer references users(id),
  primary key(id)
);

# --- !Downs

drop table short_urls;
drop table users;
