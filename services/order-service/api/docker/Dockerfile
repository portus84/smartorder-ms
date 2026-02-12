FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the jar from the build and set ownership
COPY ../target/*-service-api-*.jar app.jar
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Variable for optimizing Spring Boot in containers
ENV JAVA_OPTS="-Xms128m -Xmx512m"

# Start command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
