#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PS1="${SCRIPT_DIR}/setup-environment.ps1"

if [[ ! -f "$PS1" ]]; then
  echo "[ERROR] Could not find '$PS1'." >&2
  exit 1
fi

if ! command -v pwsh >/dev/null 2>&1; then
  echo "[ERROR] PowerShell 7 (pwsh) is required but was not found in PATH." >&2
  echo "[INFO] Install PowerShell 7: https://aka.ms/powershell-release?tag=stable" >&2
  exit 1
fi

pwsh -NoProfile -File "$PS1" "$@"
