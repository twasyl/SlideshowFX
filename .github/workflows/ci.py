import getopt
import os
import re
import shutil
import subprocess
import sys
import urllib.request


class CIEnvironment:
    build_jdk = 1
    application_jdk = 2

    def __init__(self):
        self.os_name = os.getenv('OS_NAME')
        self.home = os.getenv('USERPROFILE') if self.is_windows() else os.getenv('HOME')
        self.gradle_user_home = f'{self.home}{os.path.sep}.gradle'
        self.github_workspace = os.getenv('GITHUB_WORKSPACE')
        self.build_tool_jdk_version = '15'
        self.application_jdk_version = '15'
        self.build_tool_jdk_base_download_url = 'https://download.java.net/java/GA/jdk15/779bf45e88a44cbd9ea6621d33e33db1/36/GPL/'
        self.application_jdk_base_download_url = 'https://download.java.net/java/GA/jdk15/779bf45e88a44cbd9ea6621d33e33db1/36/GPL/'
        self.java_homes = f'{self.home}{os.path.sep}.jvms'
        self.sonar_branch = re.sub('refs\\/heads\\/', '', os.getenv('GITHUB_REF', 'master'))

    def get_os(self) -> str:
        if self.is_osx():
            return 'osx'
        elif self.is_windows():
            return 'windows'
        else:
            return 'linux'

    def is_osx(self) -> bool:
        return self.os_name == 'macos-latest'

    def is_windows(self) -> bool:
        return self.os_name == 'windows-latest'

    def get_jvm_version(self, jdk_type: int) -> str:
        if jdk_type == self.build_jdk:
            return self.build_tool_jdk_version
        else:
            return self.application_jdk_version

    def get_jdk_download_url(self, jdk_type: int) -> str:
        url: str

        if jdk_type == self.build_jdk:
            url = self.build_tool_jdk_base_download_url
        else:
            url = self.application_jdk_base_download_url

        return f'{url}{self.get_jdk_archive_name(jdk_type)}'

    def get_jdk_archive_name(self, jdk_type: int) -> str:
        extension = 'zip' if self.is_windows() else 'tar.gz'
        return f'openjdk-{self.get_jvm_version(jdk_type)}_{self.get_os()}-x64_bin.{extension}'

    def get_java_installation_folder(self, jdk_type: int) -> str:
        jvm_version = self.get_jvm_version(jdk_type)
        jvm_version = re.sub('-ea\\+[0-9]+', '', jvm_version)
        installation_folder = f'jdk-{jvm_version}'

        if self.is_osx():
            installation_folder += '.jdk'

        return installation_folder

    def get_java_home(self, jdk_type: int) -> str:
        java_home = f'{self.java_homes}{os.path.sep}{self.get_java_installation_folder(jdk_type)}'

        if self.is_osx():
            java_home += '/Contents/Home'

        return java_home

    def clean_unnecessary_jdk(self):
        print('Cleaning unnecessary JDK installations')
        build_jdk = self.get_java_installation_folder(self.build_jdk)
        application_jdk = self.get_java_installation_folder(self.application_jdk)

        for jdk_dir in os.listdir(self.java_homes):
            if jdk_dir != build_jdk and jdk_dir != application_jdk:
                print(f'Cleaning {jdk_dir}')
                shutil.rmtree(f'{self.java_homes}{os.path.sep}{jdk_dir}')

    def install_jdk(self, jdk_type):
        java_home = self.get_java_home(jdk_type)

        if not os.path.isdir(self.java_homes):
            os.mkdir(self.java_homes)
        else:
            self.clean_unnecessary_jdk()

        if not os.path.isdir(java_home):
            print(f'Setup JDK {self.get_jvm_version(jdk_type)}')
            print(f'Downloading from {self.get_jdk_download_url(jdk_type)}')
            temp_file, headers = urllib.request.urlretrieve(self.get_jdk_download_url(jdk_type),
                                                            self.get_jdk_archive_name(jdk_type))
            shutil.unpack_archive(temp_file, self.java_homes)
            os.remove(temp_file)

            if os.path.isdir(java_home):
                print(f'JDK {self.get_jvm_version(jdk_type)} installed in {java_home}')
            else:
                print(f'JDK {self.get_jvm_version(jdk_type)} seems to not be installed in {java_home}')
        else:
            print(f'JDK {self.get_jvm_version(jdk_type)} already installed in {java_home}')

    def configure_gradle(self):
        if not os.path.isdir(self.gradle_user_home):
            os.mkdir(self.gradle_user_home)

        gradle_jdk = self.get_java_home(self.build_jdk).replace('\\', '\\\\')
        application_jdk = self.get_java_home(self.application_jdk).replace('\\', '\\\\')

        gradle_properties = open(f'{self.gradle_user_home}{os.path.sep}gradle.properties', 'w')
        gradle_properties.write(f'''org.gradle.java.home={gradle_jdk}
org.gradle.caching=true
build_jdk={application_jdk}
''')
        gradle_properties.close()

    def create_jvms_list(self):
        jvms_list = open(f'{self.github_workspace}{os.path.sep}jvms.list', 'w')
        jvms_list.write(f'''build={self.get_jvm_version(self.build_jdk)}
application={self.get_jvm_version(self.application_jdk)}''')
        jvms_list.close()

    def setup_build_environment(self):
        self.install_jdk(self.build_jdk)
        self.install_jdk(self.application_jdk)
        self.configure_gradle()
        self.create_jvms_list()

    def check_prerequisites(self) -> bool:
        missing_prerequisites = False

        if self.os_name is None:
            print(
                'The OS_NAME environment variable must be set. It\'s value is the name of the OS on which the CI jobs run')
            missing_prerequisites = True

        if self.github_workspace is None:
            print('The GITHUB_WORKSPACE environment variable must be set')
            missing_prerequisites = True

        return not missing_prerequisites


def usage():
    print('''Usage:
  -h, --help     displays the help
  -s, --setup    setup the build environment
  -r, --run      run a command, typically a build command''')


if __name__ == '__main__':

    try:
        opts, args = getopt.getopt(sys.argv[1:], "hsg:", ['help', 'setup', 'gradle='])
        if len(opts) == 0:
            print('Error: at least one argument is necessary')
            usage()
            sys.exit(2)
    except getopt.GetoptError as err:
        print(f'Error: {err}')
        usage()
        sys.exit(1)

    do_setup = False
    do_gradle = False

    for opt, arg in opts:
        if opt in ('-h', '--help'):
            usage()
            sys.exit(0)
        elif opt in ('-s', '--setup'):
            do_setup = True
        elif opt in ('-g', '--gradle'):
            do_gradle = True
            command = arg

    ci_env = CIEnvironment()

    if not ci_env.check_prerequisites():
        sys.exit(3)

    if do_setup:
        print('Setting up the build environment')
        ci_env.setup_build_environment()

    if do_gradle:
        env = os.environ.copy()
        env['GRADLE_USER_HOME'] = ci_env.gradle_user_home
        env['JAVA_HOME'] = ci_env.get_java_home(CIEnvironment.build_jdk)
        env['SONAR_BRANCH'] = ci_env.sonar_branch
        full_command = ('./' if not ci_env.is_windows() else '') + f'gradlew {command}'
        subprocess.run(full_command, cwd=os.getenv('GITHUB_WORKSPACE'), shell=True, check=True, env=env)
