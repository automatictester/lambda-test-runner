package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.StringContains.containsString;
import static org.testng.Assert.assertEquals;

public class HandlerTest {

    private static final File WORK_DIR = new File(System.getenv("REPO_DIR"));
    private Request request = new Request();

    @BeforeMethod
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(WORK_DIR);
    }

    @Test
    public void testHandleRequest() {
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        request.setBranch("master");
        request.setCommand("./mvnw clean test -Dtest=SmokeTest -Dmaven.repo.local=/tmp/.m2");
        request.setStoreToS3(getStoreToS3());
        Context context = null;
        Handler handler = new Handler();
        Response response = handler.handleRequest(request, context);
        assertEquals(response.getExitCode(), 0);
        assertThat(response.getOutput(), containsString("Running uk.co.automatictester.lambdatestrunner.SmokeTest"));
        assertThat(response.getOutput(), containsString("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
        assertThat(response.getS3Prefix(), startsWith(getDatePartFromPrefix()));
    }

    @Test
    public void testHandleRequestNonDefaultBranch() {
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        request.setBranch("unit-testing");
        request.setCommand("./mvnw clean test -Dtest=*SmokeTest -Dmaven.repo.local=/tmp/.m2");
        request.setStoreToS3(getStoreToS3());
        Context context = null;
        Handler handler = new Handler();
        Response response = handler.handleRequest(request, context);
        assertEquals(response.getExitCode(), 0);
        assertThat(response.getOutput(), containsString("Running TestSuite"));
        assertThat(response.getOutput(), containsString("Tests run: 2, Failures: 0, Errors: 0, Skipped: 0"));
        assertThat(response.getS3Prefix(), startsWith(getDatePartFromPrefix()));
    }

    private List<String> getStoreToS3() {
        List<String> dirsToStore = new ArrayList<>();
        dirsToStore.add("target/surefire-reports");
        dirsToStore.add("target/failsafe-reports");
        return dirsToStore;
    }

    private String getDatePartFromPrefix() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return f.format(now);
    }
}
