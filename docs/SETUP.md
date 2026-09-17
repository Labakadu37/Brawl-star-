# Setup

Install the toolchain, then point the build script at your base APK.

## 1. JDK

```bash
sudo apt install openjdk-17-jdk
java -version   # expect 17.x
```

## 2. Android SDK build-tools

Either install Android Studio and use its SDK Manager, or grab the
command-line tools:

```bash
mkdir -p ~/Android/Sdk/cmdline-tools
cd ~/Android/Sdk/cmdline-tools
curl -O https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip
mv cmdline-tools latest

export ANDROID_SDK=~/Android/Sdk
export PATH="$ANDROID_SDK/cmdline-tools/latest/bin:$PATH"

sdkmanager --sdk_root=$ANDROID_SDK "build-tools;34.0.0" "platform-tools" \
                                   "platforms;android-34" "ndk;26.1.10909125"
```

You should now have `d8`, `zipalign`, `apksigner`, `aapt2`, and `ndk-build`
on `PATH` after also adding `$ANDROID_SDK/build-tools/34.0.0` and
`$ANDROID_SDK/ndk/26.1.10909125`.

## 3. apktool + smali/baksmali

```bash
# apktool
sudo curl -L -o /usr/local/bin/apktool.jar \
    https://github.com/iBotPeaches/Apktool/releases/latest/download/apktool.jar
sudo curl -L -o /usr/local/bin/apktool \
    https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool
sudo chmod +x /usr/local/bin/apktool

# smali + baksmali
sudo curl -L -o /usr/local/bin/smali.jar \
    https://github.com/google/smali/releases/latest/download/smali.jar
sudo curl -L -o /usr/local/bin/baksmali.jar \
    https://github.com/google/smali/releases/latest/download/baksmali.jar

cat <<'EOF' | sudo tee /usr/local/bin/smali
#!/usr/bin/env bash
exec java -jar /usr/local/bin/smali.jar "$@"
EOF
cat <<'EOF' | sudo tee /usr/local/bin/baksmali
#!/usr/bin/env bash
exec java -jar /usr/local/bin/baksmali.jar "$@"
EOF
sudo chmod +x /usr/local/bin/smali /usr/local/bin/baksmali
```

## 4. Base APK

Get the split-APK bundle from a source of your choice (APKMirror is fine),
unzip the `.xapk`, and take `com.supercell.brawlstars.apk` (~39 MB). That
is the file you feed to the build script.

You will also want the `install_time_asset_pack.apk` (~1.7 GB) alongside
during install so the game has its CSVs and textures — see `INSTALL.md`.
