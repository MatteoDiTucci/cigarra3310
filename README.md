# Cigarra3310


#Deploy in Docker
- Run `sbt dist` to produce artifact `target/universal/cigarra3310-1.0-SNAPSHOT.zip`

- Extract artifact in another folder, for example `~/dpl`, running the `unzip -d ~/dpl target/universal/*-1.0-SNAPSHOT.zip && mv ~/dpl/*/* ~/dpl/ && rm ~/dpl/bin/*.bat && mv ~/dpl/bin/* ~/dpl/bin/start`

- Create in the parent folder of `~/dpl` a Dockerfile like the following:
  `FROM openjdk:8-jre
   COPY dpl /dpl
   EXPOSE 9000 9443
   CMD /dpl/bin/start -Dhttps.port=9443 -Dplay.http.secret.key=secret`

- Substitute the value `secret` of `play.http.secret.key` with the result of `sbt playGenerateSecret`

- In the parent folder of `~/dpl` run `docker build -t cigarra3310 .`

- In the folder `~/dpl` create a sqlite db running `sqlite3 cigarra3310.db`

- Create the db schema running `sqlite3 cigarra3310.db` and then 
`CREATE TABLE level (
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
CREATE INDEX level_guid_index ON level (guid);`

- Run the application using `docker run -it -p 9000:9000 -p 9443:9443 --rm cigarra3310`

- Test in the browser navigating to `localhost:9000` or `https://localhost:9443`