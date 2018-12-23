package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;
import uk.co.automatictester.lambdatestrunner.process.ProcessResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class JdkInstallerTest {

    @Test(groups = "travis")
    public void testInstallJdk() {
        Map<String, String> extraEnvVars = Collections.singletonMap("JAVA_HOME", "/tmp/jdk10");
        ProcessResult processResult = JdkInstaller.installJdk(extraEnvVars);
        assertEquals(processResult.getExitCode(), 0);
        Path path = Paths.get("/tmp/jdk10");
        assertTrue(Files.exists(path));
        String jdkInstallationLog = System.getenv("JDK_INSTALLATION_LOG");
        assertThat(new File(jdkInstallationLog), anExistingFile());
    }
}
