#Java21 SDK
FROM amazoncorretto:21-al2023-jdk

COPY target/carrentalsystem-0.0.1-SNAPSHOT.jar carrentalsystem.jar
#Run the app
ENTRYPOINT ["java","-jar","/carrentalsystem.jar"]

#Default port
EXPOSE 8080