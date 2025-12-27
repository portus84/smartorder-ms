#!/bin/sh

# Ensure the results directory exists
mkdir -p /tmp/results

echo "Starting scheduled test execution..."

# Check if there are any .jmx files in the directory
# using a more portable check for shell scripts
count=$(ls /jmeter/*.jmx 2>/dev/null | wc -l)
if [ "$count" -eq 0 ]; then
    echo "Error: No .jmx files found in /jmeter"
    exit 1
fi

for f in /jmeter/*.jmx; do
  if [ -f "$f" ]; then
    filename=$(basename "$f" .jmx)
    report_dir="/tmp/results/${filename}_report"

    echo "========================================================="
    echo "Executing: $filename"
    echo "========================================================="

    # JMeter requires the output directory to be empty or non-existent
    if [ -d "$report_dir" ]; then
        echo "Cleaning previous report directory: $report_dir"
        rm -rf "$report_dir"
    fi

    jmeter -n -t "$f" \
      -l "/tmp/results/${filename}_results.jtl" \
      -j "/tmp/results/${filename}_jmeter.log" \
      -e -o "$report_dir" \
      -JinfluxdbUrl=${INFLUXDB_URL} \
      -JinfluxdbOrg=${INFLUXDB_ORG} \
      -JinfluxdbBucket=${INFLUXDB_BUCKET} \
      -JinfluxdbToken=${INFLUXDB_TOKEN} \
      -Japplication=smartorder

    echo "Finished: $filename"
    echo "Report generated at: $report_dir"
  fi
done

echo "All tests have been completed."
tail -f /dev/null
