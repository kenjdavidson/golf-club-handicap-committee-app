#!/usr/bin/env bash

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKERFILE_PATH="${REPO_ROOT}/Dockerfile.ollama-sidecar"

IMAGE_TAG="${OLLAMA_IMAGE_TAG:-golf-club-handicap-committee-app/ollama-sidecar:dev}"
CONTAINER_NAME="${OLLAMA_CONTAINER_NAME:-golf-club-handicap-ollama}"
HOST_PORT="${OLLAMA_HOST_PORT:-11434}"
MODEL_TAG="${OLLAMA_MODEL_TAG:-llama3.2:1b}"

if [[ ! -f "${DOCKERFILE_PATH}" ]]; then
  echo "❌ Expected Dockerfile not found at: ${DOCKERFILE_PATH}" >&2
  exit 1
fi

docker build -f "${DOCKERFILE_PATH}" -t "${IMAGE_TAG}" "${REPO_ROOT}"
docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
docker run -d \
  --name "${CONTAINER_NAME}" \
  --publish "${HOST_PORT}:11434" \
  --volume ollama-data:/root/.ollama \
  "${IMAGE_TAG}"

if ! docker exec "${CONTAINER_NAME}" ollama pull "${MODEL_TAG}"; then
  echo "❌ Failed to pull model '${MODEL_TAG}'. Check container logs with: docker logs ${CONTAINER_NAME}" >&2
  exit 1
fi

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
