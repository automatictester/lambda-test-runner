package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ConfigTest {

    @Test
    public void testGetProperty() {
        String workDir = Config.getProperty("repo.dir");
        assertEquals(workDir, "/tmp/repo");
    }
}
