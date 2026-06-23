#!/usr/bin/env bash

set -euo pipefail

IMAGE_TAG="${OLLAMA_IMAGE_TAG:-golf-club-handicap-committee-app/ollama-sidecar:dev}"
CONTAINER_NAME="${OLLAMA_CONTAINER_NAME:-golf-club-handicap-ollama}"
HOST_PORT="${OLLAMA_HOST_PORT:-11434}"
MODEL_TAG="${OLLAMA_MODEL_TAG:-llama3.2:1b}"

docker build -f Dockerfile.ollama-sidecar -t "${IMAGE_TAG}" .
docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
docker run -d \
  --name "${CONTAINER_NAME}" \
  --publish "${HOST_PORT}:11434" \
  --volume ollama-data:/root/.ollama \
  "${IMAGE_TAG}"

docker exec "${CONTAINER_NAME}" ollama pull "${MODEL_TAG}"

cat <<EOF
✅ Ollama sidecar is running as container '${CONTAINER_NAME}' on http://localhost:${HOST_PORT}

Set this when launching the app:
  APP_AI_OLLAMA_BASE_URL=http://localhost:${HOST_PORT}

Recommended AI role prompt context:
  You are assisting a handicap committee member and golf professional.
  Review member scoring history to flag suspicious scoring patterns, including:
  - Missing rounds relative to known schedules
  - Strong opening holes followed by repeated double/triple bogey finishes
  - Other anomalies that may suggest manipulated handicap outcomes
EOF
