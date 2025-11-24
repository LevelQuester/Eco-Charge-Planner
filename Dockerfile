FROM node:20-alpine AS frontend-build
WORKDIR /frontend
COPY web/package*.json ./
RUN npm ci
COPY web/ .
RUN npm run build

FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-build
WORKDIR /backend
COPY api/pom.xml .
COPY api/src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
