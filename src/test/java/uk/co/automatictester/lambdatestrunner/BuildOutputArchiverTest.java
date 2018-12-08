package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import io.findify.s3mock.S3Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class BuildOutputArchiverTest {

    private static final String BUCKET = System.getenv("BUILD_OUTPUTS");
    private S3Mock s3Mock;
    private final AmazonS3 amazonS3 = AmazonS3Factory.getInstance();

    @BeforeClass
    public void setupEnv() {
        if (System.getProperty("mockS3") != null) {
            startS3Mock();
            maybeCreateBucket();
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

    @AfterClass
    public void teardown() {
        if (System.getProperty("mockS3") != null) {
            s3Mock.stop();
        }
    }

    @Test
    public void testStore() {
        String dirNonexistent = "src/test/resources/nonexistent";
        String dirA = "src/test/resources/a";
        String dirB = "src/test/resources/b";

        List<String> dirsToStore = new ArrayList<>();
        dirsToStore.add(dirA);
        dirsToStore.add(dirNonexistent);
        dirsToStore.add(dirB);

        BuildOutputArchiver archiver = new BuildOutputArchiver(System.getProperty("user.dir"));
        String commonPrefix = archiver.store(dirsToStore);
        String keyA = commonPrefix + "/" + dirA + "/a.txt";
        String keyAA = commonPrefix + "/" + dirA + "/subdir/aa.txt";
        String keyB = commonPrefix + "/" + dirB + "/b.txt";
        String keyBB = commonPrefix + "/" + dirB + "/subdir/bb.txt";

        assertEquals(getObjectAsString(keyA), "a");
        assertEquals(getObjectAsString(keyAA), "aa");
        assertEquals(getObjectAsString(keyB), "b");
        assertEquals(getObjectAsString(keyBB), "bb");
    }

    private String getObjectAsString(String key) {
        return amazonS3.getObjectAsString(BUCKET, key);
    }
}
