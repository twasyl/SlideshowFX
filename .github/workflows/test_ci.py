import os
import shutil
import unittest

import ci


class OSTestCase(unittest.TestCase):
    def test_is_osx(self):
        ci_env = ci.CIEnvironment()
        ci_env.os_name = 'macos-latest'
        self.assertTrue(ci_env.is_osx())

    def test_is_windows(self):
        ci_env = ci.CIEnvironment()
        ci_env.os_name = 'windows-latest'
        self.assertTrue(ci_env.is_windows())


class JavaInstallationFolderTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.ci_env = ci.CIEnvironment()

    def test_on_osx(self):
        self.ci_env.os_name = 'macos-latest'
        self.assertEqual('jdk-15.jdk', self.ci_env.get_java_installation_folder(ci.CIEnvironment.build_jdk))

    def test_on_windows(self):
        self.ci_env.os_name = 'windows-latest'
        self.assertEqual('jdk-15', self.ci_env.get_java_installation_folder(ci.CIEnvironment.build_jdk))

    def test_on_linux(self):
        self.ci_env.os_name = 'ubuntu-latest'
        self.assertEqual('jdk-15', self.ci_env.get_java_installation_folder(ci.CIEnvironment.build_jdk))

    def test_with_ea(self):
        self.ci_env.os_name = 'macos-latest'
        self.ci_env.build_tool_jdk_version = '15-ea+36'
        self.assertEqual('jdk-15.jdk', self.ci_env.get_java_installation_folder(ci.CIEnvironment.build_jdk))


class JDKArchiveNameTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.ci_env = ci.CIEnvironment()

    def test_archive_name_on_osx(self):
        self.ci_env.os_name = 'macos-latest'
        self.assertEqual('openjdk-15_osx-x64_bin.tar.gz', self.ci_env.get_jdk_archive_name(ci.CIEnvironment.build_jdk))

    def test_archive_name_on_windows(self):
        self.ci_env.os_name = 'windows-latest'
        self.assertEqual('openjdk-15_windows-x64_bin.zip', self.ci_env.get_jdk_archive_name(ci.CIEnvironment.build_jdk))

    def test_archive_name_on_linux(self):
        self.ci_env.os_name = 'ubuntu-latest'
        self.assertEqual('openjdk-15_linux-x64_bin.tar.gz',
                         self.ci_env.get_jdk_archive_name(ci.CIEnvironment.build_jdk))


class JavaHomeTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.ci_env = ci.CIEnvironment()
        self.ci_env.java_homes = f'.{os.path.sep}.jvms'

    def test_archive_name_on_osx(self):
        self.ci_env.os_name = 'macos-latest'
        self.assertEqual(f'.{os.path.sep}.jvms{os.path.sep}jdk-15.jdk/Contents/Home',
                         self.ci_env.get_java_home(ci.CIEnvironment.build_jdk))

    def test_archive_name_on_windows(self):
        self.ci_env.os_name = 'windows-latest'
        self.assertEqual(f'.{os.path.sep}.jvms{os.path.sep}jdk-15',
                         self.ci_env.get_java_home(ci.CIEnvironment.build_jdk))

    def test_archive_name_on_linux(self):
        self.ci_env.os_name = 'ubuntu-latest'
        self.assertEqual(f'.{os.path.sep}.jvms{os.path.sep}jdk-15',
                         self.ci_env.get_java_home(ci.CIEnvironment.build_jdk))


class CleanUnnecesaryJDKTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        os.mkdir('.jvms')
        os.mkdir('.jvms/jdk-14.jdk')
        os.mkdir('.jvms/jdk-14.0.2.jdk')
        os.mkdir('.jvms/jdk-15.jdk')

    def setUp(self) -> None:
        self.ci_env = ci.CIEnvironment()
        self.ci_env.os_name = 'macos-latest'
        self.ci_env.java_homes = f'.{os.path.sep}.jvms'
        self.ci_env.build_tool_jdk_version = '15'
        self.ci_env.application_jdk_version = '14.0.2'

    @classmethod
    def tearDownClass(cls) -> None:
        shutil.rmtree('.jvms')

    def test_jdk_removal(self):
        self.ci_env.clean_unnecessary_jdk()
        dirs = os.listdir('.jvms')
        self.assertEqual(2, len(dirs))
        self.assertTrue(dirs.index('jdk-15.jdk') >= 0)
        self.assertTrue(dirs.index('jdk-14.0.2.jdk') >= 0)


class InstallJDKTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        os.mkdir('.jvms')

    def setUp(self) -> None:
        self.ci_env = ci.CIEnvironment()
        self.ci_env.os_name = 'macos-latest'
        self.ci_env.java_homes = f'.{os.path.sep}.jvms'
        self.ci_env.build_tool_jdk_version = '15'
        self.ci_env.application_jdk_version = '15'
        self.ci_env.build_tool_jdk_base_download_url = 'https://download.java.net/java/GA/jdk15/779bf45e88a44cbd9ea6621d33e33db1/36/GPL/'

    @classmethod
    def tearDownClass(cls) -> None:
        shutil.rmtree('.jvms')

    def test_install_build_jdk(self):
        self.ci_env.install_jdk(ci.BUILD_JDK)


class ConfigureGradleTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        os.mkdir('.jvms')
        os.mkdir('.gradle')

    def setUp(self) -> None:
        self.ci_env = ci.CIEnvironment()
        self.ci_env.os_name = 'macos-latest'
        self.ci_env.java_homes = f'.{os.path.sep}.jvms'
        self.ci_env.gradle_user_home = f'.{os.path.sep}.gradle'
        self.ci_env.build_tool_jdk_version = '15'
        self.ci_env.application_jdk_version = '15'

    @classmethod
    def tearDownClass(cls) -> None:
        shutil.rmtree('.jvms')
        shutil.rmtree('.gradle')

    def test_configure_gradle(self):
        self.ci_env.configure_gradle()


class SonarBranchTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        os.environ['GITHUB_REF'] = 'refs/heads/my-branch'

    def setUp(self) -> None:
        self.ci_env = ci.CIEnvironment()

    def test_sonar_branch(self):
        self.assertEqual('my-branch', self.ci_env.sonar_branch)


if __name__ == '__main__':
    unittest.main()
