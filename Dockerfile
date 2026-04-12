FROM node:22-slim AS build-frontend

WORKDIR /frontend
COPY ../frontend/src src
COPY ../frontend/public public
COPY ../frontend/package.json package.json
COPY ../frontend/tsconfig.json tsconfig.json
COPY ../frontend/.eslint .eslint

RUN npm install
RUN npm run build

# clean unwanted files based on frontend/build.sh
WORKDIR /frontend/build
RUN rm -f static/js/*.map
RUN rm -f service-worker.js
RUN rm -f manifest.json
RUN rm -f asset-manifest.json
RUN rm -f favicon.ico


FROM maven:latest AS build-backend

WORKDIR /backend

COPY ../backend/pom.xml .
COPY ../backend/keystore.jks .
COPY ../backend/src src

# load the newly build frontend to the backend
RUN rm -rf src/main/resources/static
COPY --from=build-frontend /frontend/build src/main/resources/static

RUN mvn clean package -DskipTests


FROM gcr.io/distroless/java21-debian12

EXPOSE 80
#COPY backend/target/app.jar /app.jar
# To delegate building the jar to docker, comment the line above and uncomment the line below
COPY --from=build-backend /backend/target/*.jar app.jar

WORKDIR /

CMD ["app.jar"]
