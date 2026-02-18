# Dockerfile JMeter 5.6.3 + InfluxDB 2.x Backend Listener
FROM eclipse-temurin:11-jre

ARG JMETER_VERSION=5.6.3

# Create non-root user, install essentials, JMeter, plugin in a single RUN to reduce layers
RUN groupadd -r jmetergroup && useradd -r -g jmetergroup jmeteruser \
    && apt-get update && apt-get install -y --no-install-recommends wget unzip \
    && wget --max-redirect=0 https://downloads.apache.org/jmeter/binaries/apache-jmeter-"${JMETER_VERSION}".tgz \
    && wget --max-redirect=0 https://downloads.apache.org/jmeter/binaries/apache-jmeter-"${JMETER_VERSION}".tgz.sha512 \
    && sha512sum -c apache-jmeter-"${JMETER_VERSION}".tgz.sha512 \
    && tar -xzf apache-jmeter-"${JMETER_VERSION}".tgz -C /opt \
    && rm apache-jmeter-"${JMETER_VERSION}".tgz* \
    && wget --max-redirect=0 https://repo1.maven.org/maven2/io/github/mderevyankoaqa/jmeter-plugins-influxdb2-listener/2.8/jmeter-plugins-influxdb2-listener-2.8.jar \
       -O /opt/apache-jmeter-"${JMETER_VERSION}"/lib/ext/jmeter-plugins-influxdb2-listener-2.8.jar \
    && rm -rf /var/lib/apt/lists/*

# Set PATH
ENV PATH="/opt/apache-jmeter-${JMETER_VERSION}/bin:$PATH"

# Set working directory
WORKDIR /jmeter

# Copy the script to the root or a specific folder
COPY run-tests.sh /run-tests.sh

# Make the script executable (essential!)
RUN chmod +x /run-tests.sh

# Switch to non-root user
USER jmeteruser

ENTRYPOINT ["/bin/sh", "/run-tests.sh"]
