package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.*;

public class JdkInstallerTest {

    @Test
    public void testInstallJdk() {
        JdkInstaller.installJdk();
        Path path = Paths.get("/tmp/jdk10");
        assertTrue(Files.exists(path));
    }
}
