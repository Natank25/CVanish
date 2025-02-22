FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /builder
COPY . .
RUN mvn dependency:resolve
RUN mvn package

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /runner
COPY --from=builder /builder/target/ target/
CMD ["java", "-jar", "target/worker-jar-with-dependencies.jar"]
