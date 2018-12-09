package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class ProcessResultTest {

    @Test(groups = "local")
    public void testGetLimitedOutput() {
        ProcessResult processResult = new ProcessResult();
        processResult.setOutput("1234567890");
        assertEquals(processResult.getOutput(3), "890");
        assertEquals(processResult.getOutput(10), "1234567890");
        assertEquals(processResult.getOutput(11), "1234567890");
    }
}
