package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class ProcessRunnerTest {

    @Test
    public void testRunProcess() {
        List<String> command = Arrays.asList("./mvnw", "clean", "test", "-Dtest=SmokeTest");
        File workDir = new File(System.getProperty("user.dir"));
        int exitCode = ProcessRunner.runProcess(command, workDir);
        assertEquals(exitCode, 0);
    }
}
