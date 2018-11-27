package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class HandlerTest {

    // TODO: null
    @Test
    public void testHandleRequest() {
        Request request = new Request();
        request.setTargetDir("/tmp/lambda-test-runner/");
        request.setCommand("./mvnw clean test -Dtest=SmokeTest");
        request.setLogFile("output.log");
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");

        Handler handler = new Handler();
        handler.handleRequest(request, null);
    }
}
