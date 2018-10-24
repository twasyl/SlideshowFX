echo "Setup JDK 1.8.0_191"
jdkVersion=1.8.0_191
jdkInstallationFolder=jdk$jdkVersion
jdkArchive=jdk-8u191-linux-x64.tar.gz

mkdir -p ~/jvm
pushd ~/jvm > /dev/null

if [ ! -d "$jdkInstallationFolder" ]; then
    echo "Cleaning existing JDK installations"
    rm -rf *
	echo "Downloading JDK $jdkVersion"
	wget --quiet --continue --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u191-b12/2787e4a523244c269598db4e85c51e0c/$jdkArchive
	tar xzf $jdkArchive
	rm $jdkArchive
    ln -s $jdkInstallationFolder current
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
