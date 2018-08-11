# cigarra schema

# --- !Ups

CREATE TABLE level (
  id text PRIMARY KEY NOT NULL,
  description text NOT NULL,
  solution text NOT NULL,
  next_level_id text,
  cigarra_id text NOT NULL
);

CREATE TABLE cigarra (
  id text PRIMARY KEY NOT NULL,
  name text NOT NULL,
  first_level_id text
);

CREATE INDEX cigarra_id_index ON cigarra (id);
CREATE INDEX level_id_index ON level (id);

# --- !Downs

DROP INDEX cigarra_id_index;
DROP INDEX level_id_index;
DROP TABLE level;
DROP TABLE cigarra;