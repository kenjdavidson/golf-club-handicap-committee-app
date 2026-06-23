#!/usr/bin/env bash

set -euo pipefail

MODEL_NAME="${OLLAMA_CUSTOM_MODEL_NAME:-golf-compliance}"
MODELFILE_PATH="${OLLAMA_MODELFILE_PATH:-/opt/ollama/Modelfile}"
BASE_MODEL="${OLLAMA_BASE_MODEL:-llama3}"

ollama serve &
ollama_pid=$!

cleanup() {
  if kill -0 "${ollama_pid}" >/dev/null 2>&1; then
    kill "${ollama_pid}" >/dev/null 2>&1 || true
    wait "${ollama_pid}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

for _ in $(seq 1 60); do
  if ollama list >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! ollama list >/dev/null 2>&1; then
  echo "❌ Ollama service did not become ready in time." >&2
  exit 1
fi

if ! ollama list | awk 'NR > 1 {print $1}' | grep -Eq "^${MODEL_NAME}(:|$)"; then
  ollama pull "${BASE_MODEL}" || {
    echo "❌ Failed to pull base model '${BASE_MODEL}'." >&2
    exit 1
  }
  ollama create "${MODEL_NAME}" -f "${MODELFILE_PATH}" || {
    echo "❌ Failed to create custom model '${MODEL_NAME}' from '${MODELFILE_PATH}'." >&2
    exit 1
  }
fi

wait "${ollama_pid}"
