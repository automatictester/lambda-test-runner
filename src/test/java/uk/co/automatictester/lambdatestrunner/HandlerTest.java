package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class HandlerTest {

    private Request request = new Request();

    @BeforeClass
    public void deleteDir() throws IOException {
        File workDir = new File(Config.getProperty("repo.dir"));
        FileUtils.deleteDirectory(workDir);
    }

    @BeforeClass
    public void setupRequest() {
        request.setCommand("./mvnw clean test -Dtest=SmokeTest -Dmaven.repo.local=/tmp/.m2");
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
    }

    @Test
    public void testHandleRequest() {
        Context context = null;
        Handler handler = new Handler();
        handler.handleRequest(request, context);
    }
}
