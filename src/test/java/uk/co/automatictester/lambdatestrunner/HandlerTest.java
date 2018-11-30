package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.StringContains.containsString;
import static org.testng.Assert.assertEquals;

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
        Response response = handler.handleRequest(request, context);
        assertEquals(response.getExitCode(), 0);
        assertThat(response.getOutput(), containsString("Running uk.co.automatictester.lambdatestrunner.SmokeTest"));
        assertThat(response.getOutput(), containsString("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
    }
}
