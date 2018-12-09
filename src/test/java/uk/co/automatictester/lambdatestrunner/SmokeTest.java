package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class SmokeTest {

    @Test(groups = "local")
    public void runSmokeTest() {
        assertTrue(true);
    }
}
