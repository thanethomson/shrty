# --- Fixing foreign key column names

# --- !Ups

alter table short_urls rename column created_by to created_by_id;

# --- !Downs

alter table short_urls rename column created_by_id to created_by;