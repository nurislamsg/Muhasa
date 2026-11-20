#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

BUILD_TYPE="${1:-debug}"

if [[ ! -f "./gradlew" ]]; then
  echo "Ошибка: файл ./gradlew не найден в корне проекта." >&2
  exit 1
fi

chmod +x ./gradlew

ensure_debug_keystore() {
  if [[ -f "debug.keystore" ]]; then
    return 0
  fi

  if [[ -f "debug.keystore.base64" ]]; then
    if command -v base64 >/dev/null 2>&1; then
      if base64 --decode debug.keystore.base64 > debug.keystore 2>/dev/null || base64 -D debug.keystore.base64 > debug.keystore 2>/dev/null; then
        echo "debug.keystore восстановлен из debug.keystore.base64."
        return 0
      fi
      rm -f debug.keystore
    fi
  fi

  if command -v keytool >/dev/null 2>&1; then
    keytool -genkeypair -v \
      -keystore debug.keystore \
      -storepass android \
      -alias androiddebugkey \
      -keypass android \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000 \
      -dname "CN=Android Debug,O=Android,C=US"
    echo "Создан новый debug.keystore."
  else
    echo "Внимание: debug.keystore не найден, и keytool недоступен." >&2
  fi
}

case "$BUILD_TYPE" in
  debug)
    ensure_debug_keystore
    echo "Сборка Debug APK..."
    ./gradlew :app:assembleDebug
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    ;;
  release)
    if [[ -z "${KEYSTORE_PATH:-}" && ! -f "my-upload-key.jks" ]]; then
      echo "Для release-сборки укажите KEYSTORE_PATH, STORE_PASSWORD и KEY_PASSWORD или поместите my-upload-key.jks в корень проекта." >&2
      exit 1
    fi
    echo "Сборка Release APK..."
    ./gradlew :app:assembleRelease
    APK_PATH="app/build/outputs/apk/release/app-release.apk"
    ;;
  *)
    echo "Использование: $0 [debug|release]" >&2
    exit 1
    ;;
esac

if [[ -f "$APK_PATH" ]]; then
  echo "Готово! APK собран: $SCRIPT_DIR/$APK_PATH"
else
  echo "Сборка завершена. Проверьте папку app/build/outputs/apk/."
fi
