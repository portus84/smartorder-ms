FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar from the build
COPY ../target/*-service-api-*.jar app.jar

# Variable for optimizing Spring Boot in containers
ENV JAVA_OPTS="-Xms128m -Xmx512m"

# Start command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
