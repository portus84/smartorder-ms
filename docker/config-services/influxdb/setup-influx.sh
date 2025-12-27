#!/bin/bash
set -e

echo "Cleaning up old data..."
rm -rf /var/lib/influxdb2/*
rm -rf /etc/influxdb2/*
rm -rf /root/.influxdbv2/*

influxd &

echo "Waiting for InfluxDB to start..."
until curl -s http://localhost:8086/health | grep -q "pass"; do
  sleep 2
done

RESPONSE=$(curl -s http://localhost:8086/api/v2/setup)

if echo "$RESPONSE" | grep -Eq '"allowed"\s*:\s*true'; then
    echo "InfluxDB is fresh. Running setup..."

    influx setup --username "$INFLUXDB_ADMIN_USER" \
                 --password "$INFLUXDB_ADMIN_PASSWORD" \
                 --org "$INFLUXDB_ORG" \
                 --bucket "$INFLUXDB_BUCKET" \
                 --token "$INFLUXDB_TOKEN" \
                 --force

    echo "Creating DBRP mapping..."
    sleep 2
    BUCKET_ID=$(influx bucket list --name "$INFLUXDB_BUCKET" --org "$INFLUXDB_ORG" --hide-headers | awk '{print $1}')

    if [ -z "$BUCKET_ID" ]; then
        echo "Error: Could not find ID for bucket $INFLUXDB_BUCKET"
    else
        influx v1 dbrp create \
            --db "$INFLUXDB_BUCKET" \
            --rp autogen \
            --bucket-id "$BUCKET_ID" \
            --org "$INFLUXDB_ORG" \
            --token "$INFLUXDB_TOKEN" \
            --default
        echo "DBRP mapping 'jmeter' <-> 'autogen' created."
    fi
else
    echo "InfluxDB already initialized. Skipping setup."
fi

echo "InfluxDB is ready and running."
wait
