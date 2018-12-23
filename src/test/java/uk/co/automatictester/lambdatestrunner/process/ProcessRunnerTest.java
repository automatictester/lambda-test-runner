package uk.co.automatictester.lambdatestrunner.process;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.testng.Assert.assertEquals;

public class ProcessRunnerTest {

    private static final int MAX_OUTPUT_SIZE = 1024;

    @Test(groups = "local")
    public void testRunProcess() {
        List<String> command = Arrays.asList("./mvnw", "clean", "test", "-Dtest=SmokeTest");
        File workDir = new File(System.getProperty("user.dir"));
        String relativeLogFile = "output.log";
        String absoluteLogFile = workDir.toString() + "/" + relativeLogFile;
        ProcessResult processResult = ProcessRunner.runProcess(command, workDir, Collections.emptyMap(), relativeLogFile);
        assertEquals(processResult.getExitCode(), 0);
        assertThat(new File(absoluteLogFile), anExistingFile());
        assertThat(processResult.getOutput(MAX_OUTPUT_SIZE), containsString("Running uk.co.automatictester.lambdatestrunner.SmokeTest"));
        assertThat(processResult.getOutput(MAX_OUTPUT_SIZE), containsString("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
        assertThat(processResult.getOutput(MAX_OUTPUT_SIZE), containsString("\n"));
    }

    @Test(groups = "local", expectedExceptions = RuntimeException.class)
    public void testRunProcessEx() {
        List<String> command = Collections.singletonList("uname -a");
        File workDir = new File("nonexistent");
        String logFile = "output.log";
        ProcessRunner.runProcess(command, workDir, Collections.emptyMap(), logFile);
    }
}
