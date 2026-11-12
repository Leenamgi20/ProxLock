#!/bin/sh
# Builds dist/ProxLock.apk from source with only an Android SDK + JDK (no Gradle).
#   ANDROID_SDK  : SDK root containing build-tools/<BUILD_TOOLS> and platforms/android-32
#   JAVA_HOME    : JDK 11+
#   KEYSTORE / KS_PASS : signing key (created automatically if missing)
# Defaults point at Unity 2022.3's bundled Android SDK/JDK.
set -e
cd "$(dirname "$0")"
UNITY_AP="/c/Program Files/Unity/Hub/Editor/2022.3.22f1/Editor/Data/PlaybackEngines/AndroidPlayer"
ANDROID_SDK="${ANDROID_SDK:-$UNITY_AP/SDK}"
JAVA_HOME="${JAVA_HOME:-$UNITY_AP/OpenJDK}"
BUILD_TOOLS="${BUILD_TOOLS:-32.0.0}"
KEYSTORE="${KEYSTORE:-proxlock.keystore}"
KS_PASS="${KS_PASS:-proxlock}"
BT="$ANDROID_SDK/build-tools/$BUILD_TOOLS"; JAR="$ANDROID_SDK/platforms/android-32/android.jar"; J="$JAVA_HOME/bin"
rm -rf build; mkdir -p build/classes build/dex dist
"$BT/aapt2" link -o build/base.apk -I "$JAR" --manifest app/AndroidManifest.xml 2>/dev/null || "$BT/aapt2.exe" link -o build/base.apk -I "$JAR" --manifest app/AndroidManifest.xml
"$J/javac" -source 8 -target 8 -nowarn -encoding UTF-8 -classpath "$JAR" -d build/classes app/src/com/proxlock/*.java
"$J/java" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --min-api 29 --lib "$JAR" --output build/dex $(find build/classes -name '*.class')
cp build/base.apk build/unsigned.apk
(cd build/dex && "$J/jar" uf ../unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk 2>/dev/null || "$BT/zipalign.exe" -f 4 build/unsigned.apk build/aligned.apk
[ -f "$KEYSTORE" ] || "$J/keytool" -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KS_PASS" -alias proxlock -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=ProxLock" >/dev/null
"$J/java" -jar "$BT/lib/apksigner.jar" sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --out dist/ProxLock.apk build/aligned.apk
rm -f dist/ProxLock.apk.idsig
echo "Built: $(pwd)/dist/ProxLock.apk"

# ---- Android phone installer (embeds dist/ProxLock.apk) ----
I=android-installer; rm -rf build/i; mkdir -p build/i/classes build/i/dex build/i/assets
cp dist/ProxLock.apk build/i/assets/
"$BT/aapt2" link -o build/i/base.apk -I "$JAR" --manifest $I/AndroidManifest.xml -A build/i/assets -0 apk 2>/dev/null || "$BT/aapt2.exe" link -o build/i/base.apk -I "$JAR" --manifest $I/AndroidManifest.xml -A build/i/assets -0 apk
"$J/javac" -source 8 -target 8 -nowarn -encoding UTF-8 -classpath "$JAR" -d build/i/classes $(find $I/src -name '*.java')
"$J/java" -cp "$BT/lib/d8.jar" com.android.tools.r8.D8 --min-api 24 --lib "$JAR" --output build/i/dex $(find build/i/classes -name '*.class')
cp build/i/base.apk build/i/unsigned.apk
(cd build/i/dex && "$J/jar" uf ../unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/i/unsigned.apk build/i/aligned.apk 2>/dev/null || "$BT/zipalign.exe" -f 4 build/i/unsigned.apk build/i/aligned.apk
"$J/java" -jar "$BT/lib/apksigner.jar" sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --out dist/ProxLock-Installer.apk build/i/aligned.apk
rm -f dist/ProxLock-Installer.apk.idsig
echo "Built: $(pwd)/dist/ProxLock-Installer.apk"
