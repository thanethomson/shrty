# --- Adding sessions table to database.

# --- !Ups

create table sessions (
  id bigserial,
  user_id integer references users(id),
  started timestamp with time zone,
  expires timestamp with time zone,
  session_key varchar(200),
  expired boolean,
  primary key(id)
);

# --- !Downs

drop table sessions;