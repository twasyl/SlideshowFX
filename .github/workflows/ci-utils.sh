function getOS() {
  if [ "$OS_NAME" == "macos-latest" ]; then
    echo -n osx
  elif [ "$OS_NAME" == "windows-latest" ]; then
    echo -n windows
  else
    echo -n linux
  fi
}

function isOSX() {
  if [ "$(getOS)" == "osx" ]; then
    echo -n true
  else
    echo -n false
  fi
}

function isWindows() {
  if [ "$(getOS)" == "windows" ]; then
    echo -n true
  else
    echo -n false
  fi
}

function convertPath() {
  if [ "$OS_NAME" == "windows-latest" ]; then
    echo -n $(cygpath -w $1 | sed -E 's/\\/\\\\/g')
  else
    echo -n $1
  fi
}

function getJDKArchiveName() {
  if [ "$(isWindows)" == "true" ]; then
    extension=zip
  else
    extension=tar.gz
  fi
  echo -n "openjdk-$(determineJvmVersion $1)_$(getOS)-x64_bin.$extension"
}

function determineJvmVersion() {
  if [ "$1" == "build" ]; then
    echo -n $BUILD_TOOL_JDK_VERSION
  else
    echo -n $APPLICATION_JDK_VERSION
  fi
}

function getJavaInstallationFolder() {
  jvmVersion=$(determineJvmVersion $1)
  jvmVersion=$(echo -n $jvmVersion | sed -E 's/-ea\+[0-9]+//')
  installationFolder="jdk-$jvmVersion"

  if [ "$(getOS)" == "osx" ]; then
    installationFolder="$installationFolder.jdk"
  fi

  echo -n "$installationFolder"
}

function getJavaHome() {
  installationFolder=$(getJavaInstallationFolder $1)

  if [ "$(isOSX)" == "true" ]; then
    echo -n "$JVMS_DIR/$installationFolder/Contents/Home"
  else
    echo -n "$JVMS_DIR/$installationFolder"
  fi
}

function cleanJDKInstallations() {
  echo "Cleaning previous JDK installations"
  jdks=$(ls $JVMS_DIR)
  buildJdk="$(getJavaInstallationFolder build)"
  applicationJdk="$(getJavaInstallationFolder application)"

  for jdk in $jdks; do
    if [ "$jdk" != "$buildJdk" ] && [ "$jdk" != "$applicationJdk" ]; then
      echo "Cleaning $jdk"
      rm -rf $JVMS_DIR/$jdk
    fi
  done
}

function setupJDK() {
  if [ ! -d "$JVMS_DIR" ]; then
    mkdir -p "$JVMS_DIR"
  fi

  cleanJDKInstallations

  for jvmType in "$@"; do
    if [ "$jvmType" == "build" ]; then
      downloadUrl=$BUILD_TOOL_JDK_BASE_DOWNLOAD_URL
    else
      downloadUrl=$APPLICATION_JDK_BASE_DOWNLOAD_URL
    fi

    jvmVersion=$(determineJvmVersion $jvmType)
    archiveName=$(getJDKArchiveName $jvmType)
    javaHome=$(getJavaHome $jvmType)

    if [ ! -d "$javaHome" ]; then
      echo "Setup JDK $jvmVersion"
      mkdir -p $JVMS_DIR
      pushd $JVMS_DIR >/dev/null
      echo "Downloading JDK $jvmVersion from $downloadUrl"
      curl -s $downloadUrl --output $archiveName
      if [ "$(getOS)" == "windows" ]; then
        7z x $archiveName -y > nul
      else
        tar xzf $archiveName
      fi
      rm $archiveName
      popd >/dev/null
      echo "JDK $jvmVersion installed in $javaHome"
      $javaHome/bin/java -version
    else
      echo "JDK $jvmVersion already installed in $javaHome"
    fi
  done
}

function getJavaOptions() {
  if [ "$(isOSX)" == "true" ]; then
    echo -n "-Djava.awt.headless=true -Dtestfx.robot=glass -Dtestfx.headless=true -Dprism.order=sw -Dprism.verbose=true"
  elif [ "$(isWindows)" == "true" ]; then
    echo -n "-Djava.awt.headless=true -Dtestfx.robot=glass -Dtestfx.headless=true -Dprism.order=sw -Dprism.text=t2k"
  else
    echo -n "-Djava.awt.headless=true -Dtestfx.robot=glass -Dtestfx.headless=true -Dprism.order=sw"
  fi
}

function configureGradle() {
  mkdir -p ~/.gradle
  echo org.gradle.java.home=$(convertPath $(getJavaHome build)) >> ~/.gradle/gradle.properties
  echo build_jdk=$(convertPath $(getJavaHome application)) >> ~/.gradle/gradle.properties
}

JVMS_DIR=$HOME/jvm
BUILD_TOOL_JDK_VERSION=14.0.1
BUILD_TOOL_JDK_BASE_DOWNLOAD_URL="https://download.java.net/java/GA/jdk14.0.1/664493ef4a6946b186ff29eb326336a2/7/GPL/$(getJDKArchiveName build)"
APPLICATION_JDK_VERSION=14.0.1
APPLICATION_JDK_BASE_DOWNLOAD_URL="https://download.java.net/java/GA/jdk14.0.1/664493ef4a6946b186ff29eb326336a2/7/GPL/$(getJDKArchiveName application)"
