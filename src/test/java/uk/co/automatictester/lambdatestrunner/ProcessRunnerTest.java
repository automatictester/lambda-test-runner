package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ProcessRunnerTest {

    // TODO: null
    @Test
    public void testRunProcess() {
        List<String> command = Arrays.asList("./mvnw", "clean", "test", "-Dtest=SmokeTest");
        ProcessRunner.runProcess(command, null);
    }
}
