# --- Rename the table columns to work with Ebean

# --- !Ups

alter table users rename column firstName to first_name;
alter table users rename column lastName to last_name;
alter table users rename column passwordHash to password_hash;

alter table short_urls rename column shortCode to short_code;
alter table short_urls rename column hitCount to hit_count;
alter table short_urls rename column createdBy to created_by;

# --- !Downs

alter table users rename column first_name to firstName;
alter table users rename column last_name to lastName;
alter table users rename column password_hash to passwordHash;

alter table short_urls rename column short_code to shortCode;
alter table short_urls rename column hit_count to hitCount;
alter table short_urls rename column created_by to createdBy;
