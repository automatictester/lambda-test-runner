package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ProcessConfigTest {

    @Test
    public void testGetWorkDir() {
        ProcessConfig processConfig = new ProcessConfig();
        String workDir = processConfig.getWorkDir().toString();
        assertEquals(workDir, "/tmp/lambda-test-runner");
    }
}
