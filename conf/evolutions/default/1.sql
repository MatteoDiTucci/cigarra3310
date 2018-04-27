# cigarra schema

# --- !Ups

CREATE TABLE level (
  guid text PRIMARY KEY NOT NULL,
  description text NOT NULL,
  solution text NOT NULL,
  next_level_guid text,
  cigarra_guid text NOT NULL
);

CREATE TABLE cigarra (
  guid text PRIMARY KEY NOT NULL,
  name text NOT NULL,
  first_level_guid text
);

CREATE INDEX cigarra_guid_index ON cigarra (guid);
CREATE INDEX level_guid_index ON level (guid);

# --- !Downs

DROP INDEX cigarra_guid_index;
DROP INDEX level_guid_index;
DROP TABLE level;
DROP TABLE cigarra;