package gradle.cucumber;

import java.io.File;

public class AbstractConfigurationTest {
    String initialJavaIoTmpdir;

    protected void changeJavaTmpDirForTests() {
        initialJavaIoTmpdir = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", new File("build/test-execution").getAbsolutePath());
    }

    protected void resetJavaTmpDirForTests() {
        System.setProperty("java.io.tmpdir", initialJavaIoTmpdir);
    }
}
