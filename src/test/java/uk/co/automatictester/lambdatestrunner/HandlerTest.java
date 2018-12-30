package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.findify.s3mock.S3Mock;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import uk.co.automatictester.lambdatestrunner.request.Request;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.StringContains.containsString;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class HandlerTest {

    private final String bucket = System.getenv("BUILD_OUTPUTS");
    private final File workDir = new File(System.getenv("REPO_DIR"));
    private final AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
    private final S3Mock s3Mock = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();
    private Request rawRequest = new Request();

    @BeforeClass(alwaysRun = true)
    public void setupEnv() {
        if (System.getProperty("mockS3") != null) {
            startS3Mock();
            maybeCreateBucket();
        }
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        if (System.getProperty("mockS3") != null) {
            s3Mock.stop();
        }
    }

    private void startS3Mock() {
        s3Mock.start();
    }

    private void maybeCreateBucket() {
        if (!amazonS3.doesBucketExistV2(bucket)) {
            amazonS3.createBucket(bucket);
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(workDir);
    }

    @Test(groups = "local")
    public void testHandleRequest() {
        rawRequest.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        rawRequest.setBranch("master");
        rawRequest.setCommand("./mvnw clean test -Dtest=SmokeTest -Dmaven.repo.local=${MAVEN_USER_HOME}");
        rawRequest.setStoreToS3(getStoreToS3());
        Context context = null;
        Handler handler = new Handler();
        Response response = handler.handleRequest(rawRequest, context);
        assertEquals(response.getExitCode(), 0);
        assertThat(response.getOutput(), containsString("Running uk.co.automatictester.lambdatestrunner.SmokeTest"));
        assertThat(response.getOutput(), containsString("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
        assertThat(response.getS3Prefix(), startsWith(getDatePartFromPrefix()));
        String surefireZipFileS3Key = response.getS3Prefix() + "/target/surefire-reports.zip";
        assertTrue(amazonS3.doesObjectExist(bucket, surefireZipFileS3Key));
        String testExecutionLogS3Key = response.getS3Prefix() + "/" + System.getenv("TEST_EXECUTION_LOG");
        assertTrue(amazonS3.doesObjectExist(bucket, testExecutionLogS3Key));
    }

    @Test(groups = "local")
    public void testHandleRequestNonDefaultBranch() {
        rawRequest.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        rawRequest.setBranch("unit-testing");
        rawRequest.setCommand("./mvnw clean test -Dtest=*SmokeTest -Dmaven.repo.local=${MAVEN_USER_HOME}");
        rawRequest.setStoreToS3(getStoreToS3());
        Context context = null;
        Handler handler = new Handler();
        Response response = handler.handleRequest(rawRequest, context);
        assertEquals(response.getExitCode(), 0);
        assertThat(response.getOutput(), containsString("Running TestSuite"));
        assertThat(response.getOutput(), containsString("Tests run: 2, Failures: 0, Errors: 0, Skipped: 0"));
        assertThat(response.getS3Prefix(), startsWith(getDatePartFromPrefix()));
        String surefireZipFileS3Key = response.getS3Prefix() + "/target/surefire-reports.zip";
        assertTrue(amazonS3.doesObjectExist(bucket, surefireZipFileS3Key));
        String testExecutionLogS3Key = response.getS3Prefix() + "/" + System.getenv("TEST_EXECUTION_LOG");
        assertTrue(amazonS3.doesObjectExist(bucket, testExecutionLogS3Key));
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
