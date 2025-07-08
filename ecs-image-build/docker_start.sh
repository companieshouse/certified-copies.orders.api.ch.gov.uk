#!/bin/bash

# Start script for certified-copies.orders.api.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "certified-copies.orders.api.ch.gov.uk.jar"
