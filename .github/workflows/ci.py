import getopt
import os
import re
import subprocess
import sys


class CIEnvironment:

    def __init__(self):
        self.os_name = os.getenv('OS_NAME')
        self.home = os.getenv('USERPROFILE') if self.is_windows() else os.getenv('HOME')
        self.gradle_user_home = f'{self.home}{os.path.sep}.gradle'
        self.github_workspace = os.getenv('GITHUB_WORKSPACE')
        self.java_homes = f'{self.gradle_user_home}{os.path.sep}jdks'
        self.sonar_branch = re.sub('refs\\/heads\\/', '', os.getenv('GITHUB_REF', 'master'))

    def get_os(self) -> str:
        if self.is_osx():
            return 'osx'
        elif self.is_windows():
            return 'windows'
        else:
            return 'linux'

    def is_osx(self) -> bool:
        return self.os_name.startswith('macos-')

    def is_windows(self) -> bool:
        return self.os_name.startswith('windows-')

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
  -g, --gradle   run a gradle command''')


if __name__ == '__main__':

    try:
        opts, args = getopt.getopt(sys.argv[1:], "hg:", ['help', 'gradle='])
        if len(opts) == 0:
            print('Error: at least one argument is necessary')
            usage()
            sys.exit(2)
    except getopt.GetoptError as err:
        print(f'Error: {err}')
        usage()
        sys.exit(1)

    do_gradle = False

    for opt, arg in opts:
        if opt in ('-h', '--help'):
            usage()
            sys.exit(0)
        elif opt in ('-g', '--gradle'):
            do_gradle = True
            command = arg

    ci_env = CIEnvironment()

    if not ci_env.check_prerequisites():
        sys.exit(3)

    if do_gradle:
        env = os.environ.copy()
        env['GRADLE_USER_HOME'] = ci_env.gradle_user_home
        env['SONAR_BRANCH'] = ci_env.sonar_branch
        full_command = (
                           './' if not ci_env.is_windows() else '') + f'gradlew --build-cache -Dorg.gradle.jvmargs="-Xmx2g -XX:MaxMetaspaceSize=1g -Dfile.encoding=UTF-8" {command}'
        subprocess.run(full_command, cwd=os.getenv('GITHUB_WORKSPACE'), shell=True, check=True, env=env)
