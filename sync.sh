#!/usr/bin/env bash
# Compila, instala e sincroniza com o GitHub em um comando.
set -e
cd "$(dirname "$0")"

MSG="${1:-chore: atualiza build}"

echo "==> Compilando..."
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew assembleDebug 2>&1 | grep -E "^e: |BUILD" | head -30

echo "==> Instalando no dispositivo..."
adb install -r app/build/outputs/apk/debug/NoidVideo-0.1.0-debug.apk

echo "==> Sincronizando git..."
git add -A
if git diff --cached --quiet; then
  echo "Nada para commitar."
else
  git commit -m "$MSG"
  git push
fi

echo "==> Pronto."
