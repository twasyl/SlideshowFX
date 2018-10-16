echo "Setup JDK 1.8.0_181"
jdkVersion=1.8.0_181
jdkInstallationFolder=jdk$jdkVersion
jdkArchive=jdk-8u181-linux-x64.tar.gz

mkdir -p ~/jvm
pushd ~/jvm > /dev/null

if [ ! -d "$jdkInstallationFolder" ]; then
	echo "Downloading JDK $jdkVersion"
	wget --quiet --continue --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u181-b13/96a7b8442fe848ef90c96a2fad6ed6d1/$jdkArchive
	tar xzf $jdkArchive
	rm $jdkArchive
	pushd $jdkInstallationFolder > /dev/null
	rm -f src.zip
	rm -f javafx-src.zip
	rm -f COPYRIGHT
	rm -f LICENSE
	rm -f README.html
	rm -f release
	rm -f THIRDPARTY*
	echo "JDK $jdkVersion installed in `pwd`"
	popd > /dev/null
else
	echo "JDK $jdkVersion already installed in `pwd`/$jdkInstallationFolder"
fi

popd > /dev/null
