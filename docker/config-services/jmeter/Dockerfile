# Dockerfile JMeter 5.6.3 + InfluxDB 2.x Backend Listener
FROM eclipse-temurin:11-jre

ARG JMETER_VERSION=5.6.3

# Install essentials
RUN apt-get update && apt-get install -y wget unzip \
    && rm -rf /var/lib/apt/lists/*

# Install JMeter
RUN wget https://downloads.apache.org/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz \
    && tar -xzf apache-jmeter-${JMETER_VERSION}.tgz -C /opt \
    && rm apache-jmeter-${JMETER_VERSION}.tgz

# Download InfluxDB 2.x Backend Listener plugin
RUN wget https://repo1.maven.org/maven2/io/github/mderevyankoaqa/jmeter-plugins-influxdb2-listener/2.8/jmeter-plugins-influxdb2-listener-2.8.jar \
    -O /opt/apache-jmeter-${JMETER_VERSION}/lib/ext/jmeter-plugins-influxdb2-listener-2.8.jar

# Set PATH
ENV PATH="/opt/apache-jmeter-${JMETER_VERSION}/bin:$PATH"

# Set working directory
WORKDIR /jmeter

# Copy the script to the root or a specific folder
COPY run-tests.sh /run-tests.sh

# Make the script executable (essential!)
RUN chmod +x /run-tests.sh

ENTRYPOINT ["/bin/sh", "/run-tests.sh"]
