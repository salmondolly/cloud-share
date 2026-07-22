#!/usr/bin/env sh
set -eu

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Checking backend health..."
curl --fail --silent "$BASE_URL/actuator/health" | grep -q '"status":"UP"'

echo "Checking public plans endpoint..."
curl --fail --silent "$BASE_URL/api/plans" | grep -q 'PREMIUM'

echo "Smoke test passed."
