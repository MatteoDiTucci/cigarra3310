FROM openjdk:8-jre
COPY dpl /dpl
EXPOSE 9000 9443
CMD /dpl/bin/start -Dhttps.port=9443 -Dplay.http.secret.key=<use sbt playGenerateSecret to generate this value>