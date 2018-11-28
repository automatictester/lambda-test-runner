package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ProcessRunnerTest {

    private ProcessConfig processConfig = mock(ProcessConfig.class);

    @BeforeClass
    public void mockGetWorkDir() {
        when(processConfig.getWorkDir()).thenReturn(new File(System.getProperty("user.dir")));
    }

    @Test
    public void testRunProcess() {
        ProcessRunner processRunner = new ProcessRunner(processConfig);
        List<String> command = Arrays.asList("./mvnw", "clean", "test", "-Dtest=SmokeTest");
        int exitCode = processRunner.runProcess(command);
        assertEquals(exitCode, 0);
    }
}
