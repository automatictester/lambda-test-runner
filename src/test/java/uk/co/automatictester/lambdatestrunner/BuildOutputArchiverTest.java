package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import io.findify.s3mock.S3Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class BuildOutputArchiverTest {

    private final String s3Bucket = System.getenv("BUILD_OUTPUTS");
    private final String workDir = System.getProperty("user.dir");
    private final AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
    private final String commonS3Prefix = getS3Prefix();
    private final S3Mock s3Mock = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();

    @BeforeClass(alwaysRun = true)
    public void setupEnv() {
        if (System.getProperty("mockS3") != null) {
            startS3Mock();
            maybeCreateBucket();
        }
    }

    private void startS3Mock() {
        s3Mock.start();
    }

    private void maybeCreateBucket() {
        if (!amazonS3.doesBucketExistV2(s3Bucket)) {
            amazonS3.createBucket(s3Bucket);
        }
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        if (System.getProperty("mockS3") != null) {
            s3Mock.stop();
        }
    }

    @Test(groups = "local")
    public void testStore() {
        String dirNonexistent = "src/test/resources/nonexistent";
        String dirA = "src/test/resources/a";
        String dirB = "src/test/resources/b";

        List<String> dirsToStore = new ArrayList<>();
        dirsToStore.add(dirA);
        dirsToStore.add(dirNonexistent);
        dirsToStore.add(dirB);


        BuildOutputArchiver archiver = new BuildOutputArchiver(workDir, commonS3Prefix);
        archiver.store(dirsToStore);
        String keyA = commonS3Prefix + "/" + dirA + "/a.txt";
        String keyAA = commonS3Prefix + "/" + dirA + "/subdir/aa.txt";
        String keyB = commonS3Prefix + "/" + dirB + "/b.txt";
        String keyBB = commonS3Prefix + "/" + dirB + "/subdir/bb.txt";

        assertEquals(getObjectAsString(keyA), "a");
        assertEquals(getObjectAsString(keyAA), "aa");
        assertEquals(getObjectAsString(keyB), "b");
        assertEquals(getObjectAsString(keyBB), "bb");
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testStoreNullInput() {
        BuildOutputArchiver archiver = new BuildOutputArchiver(workDir, commonS3Prefix);
        archiver.store(null);
    }

    private String getS3Prefix() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        return f.format(now);
    }

    private String getObjectAsString(String key) {
        return amazonS3.getObjectAsString(s3Bucket, key);
    }
}
