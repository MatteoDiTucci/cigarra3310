# Cigarra3310

## Description
Create a sequence of riddles and let other people play it.  
The name Cigarra is a homage to the series of Cicada3301 puzzles.  
The number 3310 is a homage to the Nokia 3310 mobile phone.  

# Run locally
- Create a local database instance following [these documentation]()
- Test with `sbt test`
- Run with `sbt -Dconfig.resource=local.conf run`

## Create database
- Prerequisites: mysql 5.7 installed
- Once logged inside mysql as root, give: 

  `CREATE DATABASE cigarra3310db;
   
   USE cigarra3310db;
   
   CREATE USER 'cigarra'@'localhost' IDENTIFIED BY '<PASSWORD_PLACEHOLDER>';
   
   GRANT DELETE, UPDATE, INSERT, SELECT ON cigarra3310db.* TO 'cigarra'@'localhost';
   
   FLUSH PRIVILEGES;
   
   CREATE TABLE level (
     id                VARCHAR(36)    PRIMARY KEY NOT NULL,
     description       TEXT                       NOT NULL,
     solution          TEXT                       NOT NULL,
     next_level_id     VARCHAR(36)                        ,
     cigarra_id        VARCHAR(36)                NOT NULL
   );
   
   CREATE TABLE cigarra (
     id                VARCHAR(36)    PRIMARY KEY NOT NULL,
     name              TEXT                       NOT NULL,
     first_level_id    VARCHAR(36)
   );
   
   CREATE INDEX cigarra_id_index ON cigarra (id);
   
   CREATE INDEX level_id_index ON level (id);`
 
 
 ## Create environment in AWS Beanstalk
 To setup the AWS infrastructure:
 - For allowing the database creation, go to AWS Console -> IAM -> Roles -> Create Role -> AWSServiceRoleForRDS
 - Go to AWS Console -> Beanstalk
 - Click Create Environment
 - Choose Web Server Environment
 - Pick Docker as platform
 - Click Configure More Options
 - In Database section choose mysql, 5.7.22, 5GB
 - Click create environment
 - Pick the sample application for now

More info [here](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/using-features.environments.html)


## Create DB schema in AWS Beanstalk RDS
- Prerequisites: [aws eb ssh setup](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb3-ssh.html)

- ssh into the EC2 instance with `eb ssh <beanstalk-environment-name> --region <beanstalk-environment-region> --profile eb-cli`

- Install mysql with `sudo yum install mysql-server mysql-client`

- Database username and password are the ones chosen when creating the Beanstalk environment.  
  The RDS endpoint can be retrieved going to AWS Console -> RDS -> Instances -> choose the instance -> Connect section.  
  Connect with database with `mysql -h<rds_endpoint> -u<root_username> -p`

- Create the database schema using the above ##Create database section

 
 ## Deploy on AWS Beanstalk environment
 - Run `sbt dist` to produce the artifact `target/universal/cigarra3310-1.0-SNAPSHOT.zip`
 
 - Extract artifact in another folder, for example `~/cigarra3310/dpl`, with
 `mkdir -p ~/cigarra3310/dpl && 
  rm -R ~/cigarra3310/* && 
  unzip -d ~/cigarra3310/dpl target/universal/*-1.0-SNAPSHOT.zip && 
  mv ~/cigarra3310/dpl/*/* ~/cigarra3310/dpl/ && 
  rm ~/cigarra3310/dpl/bin/*.bat && 
  mv ~/cigarra3310/dpl/bin/* ~/cigarra3310/dpl/bin/start && 
  cp -R .ebextensions ~/cigarra3310/ &&  
  cp Dockerfile ~/cigarra3310/`
 
 - Inside `~/cigarra3310/Dockerfile` substitute the value of `play.http.secret.key` with the result of `sbt playGenerateSecret`.
   Be mindful to remove any symbol which is not a number nor a alphabetical character
   
- In `~/cigarra3310` create a compressed file containing `dpl/` and `Dockerfile`

- Upload the archive through AWS Beanstalk Console: AWS Console -> Beanstalk -> <Beanstalk_Environment_Name> -> click Upload and Deploy
