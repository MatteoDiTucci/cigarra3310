# Cigarra3310


# Create application artifact to be deployed 
- Run `sbt dist` to produce the artifact `target/universal/cigarra3310-1.0-SNAPSHOT.zip`

- Extract artifact in another folder, for example `~/Desktop/cigarra3310/dpl`, with 
`mkdir ~/Desktop/cigarra3310/dpl -p && 
 rm -R ~/Desktop/cigarra3310/dpl && 
 unzip -d ~/Desktop/cigarra3310/dpl target/universal/*-1.0-SNAPSHOT.zip && 
 mv ~/Desktop/cigarra3310/dpl/*/* ~/Desktop/cigarra3310/dpl/ && 
 rm ~/Desktop/cigarra3310/dpl/bin/*.bat && 
 mv ~/Desktop/cigarra3310/dpl/bin/* ~/Desktop/cigarra3310/dpl/bin/start && 
 cp -R .ebextensions ~/Desktop/cigarra3310/
 cp Dockerfile ~/Desktop/cigarra3310/`

- Inside `~/Desktop/cigarra3310/Dockerfile` substitute the value of `play.http.secret.key` with the result of `sbt playGenerateSecret`

# Create DB to be deployed
- In folder `~/Desktop/cigarra3310/dpl` create a sqlite3 db with `sqlite3 cigarra3310.db` and then `.database`

- Adjust database permissions `chmod 770 cigarra3310.db`

- Create the db schema with `sqlite3 cigarra3310.db` and then 
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

# Run Docker container locally
- Prerequisites: Docker

- Ensure the current folder just contains `Dockerfile` and `dpl/`

- Build Docker image with `docker build -t cigarra3310 .`

- Run the application with `docker run -it -p 9000:9000 -p 9443:9443 --rm cigarra3310`

- Navigate to `localhost:9000` or `https://localhost:9443`

# Upload to AWS Beanstalk
- Prerequisites: AWS Beanstalk environment has been created
  - [aws doc](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/using-features.environments.html)
  - [kipsigman guide](https://github.com/kipsigman/play-elastic-beanstalk)
                                            
- In `~/Desktop/cigarra3310` create a compressed file containing `dpl/`, `Dockerfile` and `.ebextensions`

- Upload the archive through AWS Beanstalk Console

- Test in the browser navigating to `http://cigarra3310-env.mpxdwguh43.us-east-1.elasticbeanstalk.com` or `sturie.eu`


# Retrieve production database
- Prerequisites: eb ssh setup [aws doc](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-ssh.html)

- ssh into the EC2 instance with `eb ssh Cigarra3310-env --region us-east-1 --profile eb-cli`

- Retrieve the Docker container name with `sudo docker ps`

- Copy the sqlite3 db file outside of Docker container with `sudo docker cp <docker_container_id>:/dpl/cigarra3310.db`

- In another shell, retrieve the sqlite3 db file from the EC2 instance to local with `scp -i ~/.ssh/aws-eb-cigarra3310 ec2-user@Cigarra3310-env.mpxdwguh43.us-east-1.elasticbeanstalk.com:/home/ec2-user/cigarra3310.db ./`