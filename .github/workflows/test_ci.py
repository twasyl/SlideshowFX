import ci
import os
import unittest


class OSTestCase(unittest.TestCase):
    def test_is_osx(self):
        ci_env = ci.CIEnvironment()
        ci_env.os_name = 'macos-latest'
        self.assertTrue(ci_env.is_osx())

    def test_is_windows(self):
        ci_env = ci.CIEnvironment()
        ci_env.os_name = 'windows-latest'
        self.assertTrue(ci_env.is_windows())


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
