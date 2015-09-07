# --- Adding support for making only certain links "primary".

# --- !Ups

--- First create the is_primary field
alter table short_urls add column is_primary boolean default false;
--- Mark the latest of each URL as being primary
update short_urls set is_primary = true where id in (select max(id) from short_urls group by short_code);

# --- !Downs

alter table short_urls drop column is_primary;
