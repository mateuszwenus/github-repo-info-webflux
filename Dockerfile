FROM maven:3.8.7-eclipse-temurin-17 as build
WORKDIR /build
COPY pom.xml ./pom.xml
RUN mvn dependency:resolve
COPY src ./src
RUN mvn package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/github-repo-info-webflux*.jar ./github-repo-info-webflux.jar
EXPOSE 8080
CMD ["java", "-jar", "github-repo-info-webflux.jar"]

