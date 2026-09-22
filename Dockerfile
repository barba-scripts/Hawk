# Build
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -B -DskipTests package \
	&& cp target/hawk-*.jar /app/app.jar

# Runtime
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd -r hawk && useradd -r -g hawk hawk
USER hawk

COPY --from=build /app/app.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
