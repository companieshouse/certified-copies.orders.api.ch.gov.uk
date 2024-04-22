#!/bin/bash

# Start script for certified-copies.orders.api.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" "certified-copies.orders.api.ch.gov.uk.jar"
