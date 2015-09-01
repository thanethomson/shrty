# --- Adding a title to the ShortURL model.

# --- !Ups

alter table short_urls add column title text;

# --- !Downs

alter table short_urls drop column title;
