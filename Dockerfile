FROM maven:3.9.11-eclipse-temurin-24-noble AS build
WORKDIR /opt/app
COPY . .
RUN mvn package -Dmaven.test.skip -B -P prettierSkip,checkstyleSkip

FROM eclipse-temurin:24-jre-noble AS runtime
WORKDIR /opt/app
COPY --from=build /opt/app/otp-shaded/target/otp-shaded-*-SNAPSHOT.jar otp.jar
EXPOSE 8080
WORKDIR /var/opentripplanner
ENTRYPOINT ["java", "-jar", "/opt/app/otp.jar"]
