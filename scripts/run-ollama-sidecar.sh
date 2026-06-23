#!/usr/bin/env bash

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCKERFILE_PATH="${REPO_ROOT}/Dockerfile.ollama-sidecar"

IMAGE_TAG="${OLLAMA_IMAGE_TAG:-golf-club-handicap-committee-app/ollama-sidecar:dev}"
CONTAINER_NAME="${OLLAMA_CONTAINER_NAME:-golf-club-handicap-ollama}"
HOST_PORT="${OLLAMA_HOST_PORT:-11434}"
CUSTOM_MODEL_NAME="${OLLAMA_CUSTOM_MODEL_NAME:-golf-compliance}"

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

for _ in $(seq 1 120); do
  if docker exec "${CONTAINER_NAME}" ollama list | awk 'NR > 1 {print $1}' | grep -Eq "^${CUSTOM_MODEL_NAME}(:|$)"; then
    break
  fi
  sleep 1
done

if ! docker exec "${CONTAINER_NAME}" ollama list | awk 'NR > 1 {print $1}' | grep -Eq "^${CUSTOM_MODEL_NAME}(:|$)"; then
  echo "❌ Failed to initialize custom model '${CUSTOM_MODEL_NAME}'. Check container logs with: docker logs ${CONTAINER_NAME}" >&2
  exit 1
fi

cat <<EOF
✅ Ollama sidecar is running as container '${CONTAINER_NAME}' on http://localhost:${HOST_PORT}
✅ Custom model is ready: ${CUSTOM_MODEL_NAME}

Set this when launching the app:
  APP_AI_OLLAMA_BASE_URL=http://localhost:${HOST_PORT}

Recommended AI role prompt context:
  You are assisting a handicap committee member and golf professional.
  Review member scoring history to flag suspicious scoring patterns, including:
  - Missing rounds relative to known schedules
  - Strong opening holes followed by repeated double/triple bogey finishes
  - Other anomalies that may suggest manipulated handicap outcomes
EOF
