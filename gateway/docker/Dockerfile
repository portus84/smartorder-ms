FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar from the build
COPY ../target/gateway-*.jar app.jar

# Variable for optimizing Spring Boot in containers
ENV JAVA_OPTS="-Xms128m -Xmx512m"

# Espone la porta interna del gateway
EXPOSE 8080

# Start command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
