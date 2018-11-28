package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ConfigTest {

    @Test
    public void testGetProperty() {
        String workDir = Config.getProperty("work.dir");
        assertEquals(workDir, "/tmp/lambda-test-runner");
    }
}
