package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import org.testng.annotations.Test;

public class HandlerTest {

    // TODO: do not install JDK when running locally
    @Test
    public void testHandleRequest() {
        Request request = new Request();
        request.setTargetDir("/tmp/lambda-test-runner/");
        request.setCommand("./mvnw -pl lightning-core clean test -Dtest=SmokeTest -Dmaven.repo.local=/tmp/.m2");
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");

        Context context = null;
        Handler handler = new Handler();
        handler.handleRequest(request, context);
    }
}
