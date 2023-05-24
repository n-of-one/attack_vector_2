#FROM openjdk:11-jdk-slim-bullseye AS build-env
#COPY /target/app.jar app.jar
#WORKDIR /app
#RUN javac examples/*.java
#RUN jar cfe main.jar examples.HelloJava examples/*.class

FROM gcr.io/distroless/java17-debian11
COPY /target/app.jar /app.jar
#COPY --from=build-env /app /app
WORKDIR /
EXPOSE 80
CMD ["app.jar"]