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

public class HandlerTest {

    private static final String BUCKET = System.getenv("BUILD_OUTPUTS");
    private static final File WORK_DIR = new File(System.getenv("REPO_DIR"));
    private final AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
    private S3Mock s3Mock;
    private Request request = new Request();

    @BeforeClass(alwaysRun = true)
    public void setupEnv() {
        if (System.getProperty("mockS3") != null) {
            startS3Mock();
        }
        maybeCreateBucket();
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        maybeDeleteBucket();
        if (System.getProperty("mockS3") != null) {
            s3Mock.stop();
        }
    }

    private void startS3Mock() {
        int port = 8001;
        s3Mock = new S3Mock.Builder().withPort(port).withInMemoryBackend().build();
        s3Mock.start();
    }

    private void maybeCreateBucket() {
        if (!amazonS3.doesBucketExistV2(BUCKET)) {
            amazonS3.createBucket(BUCKET);
        }
    }

    private void maybeDeleteBucket() {
        if (amazonS3.doesBucketExistV2(BUCKET)) {
            deleteAllObjects(BUCKET);
            amazonS3.deleteBucket(BUCKET);
        }
    }

    private void deleteAllObjects(String bucket) {
        ObjectListing objectListing = amazonS3.listObjects(bucket);
        while (true) {
            Iterator<S3ObjectSummary> objIter = objectListing.getObjectSummaries().iterator();
            while (objIter.hasNext()) {
                amazonS3.deleteObject(bucket, objIter.next().getKey());
            }
            if (objectListing.isTruncated()) {
                objectListing = amazonS3.listNextBatchOfObjects(objectListing);
            } else {
                break;
            }
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(WORK_DIR);
    }

    @Test(groups = "local")
    public void testHandleRequest() {
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        request.setBranch("master");
        request.setCommand("./mvnw clean test -Dtest=SmokeTest -Dmaven.repo.local=${MAVEN_USER_HOME}");
        request.setStoreToS3(getStoreToS3());
        Context context = null;
        Handler handler = new Handler();
        Response response = handler.handleRequest(request, context);
        assertEquals(response.getExitCode(), 0);
        assertThat(response.getOutput(), containsString("Running uk.co.automatictester.lambdatestrunner.SmokeTest"));
        assertThat(response.getOutput(), containsString("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0"));
        assertThat(response.getS3Prefix(), startsWith(getDatePartFromPrefix()));
    }

    @Test(groups = "local")
    public void testHandleRequestNonDefaultBranch() {
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        request.setBranch("unit-testing");
        request.setCommand("./mvnw clean test -Dtest=*SmokeTest -Dmaven.repo.local=${MAVEN_USER_HOME}");
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
