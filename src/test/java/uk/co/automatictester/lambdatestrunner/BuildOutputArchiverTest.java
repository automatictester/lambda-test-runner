package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.findify.s3mock.S3Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BuildOutputArchiverTest {

    private final String bucket = System.getenv("BUILD_OUTPUTS");
    private final String workDir = System.getProperty("user.dir");
    private final AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
    private final String commonS3Prefix = getS3Prefix();
    private final S3Mock s3Mock = new S3Mock.Builder().withPort(8001).withInMemoryBackend().build();

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
        s3Mock.start();
    }

    private void maybeCreateBucket() {
        if (!amazonS3.doesBucketExistV2(bucket)) {
            amazonS3.createBucket(bucket);
        }
    }

    private void maybeDeleteBucket() {
        if (amazonS3.doesBucketExistV2(bucket)) {
            deleteAllObjects(bucket);
            amazonS3.deleteBucket(bucket);
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
        String keyDirA = commonS3Prefix + "/" + dirA + ".zip";
        String keyDirB = commonS3Prefix + "/" + dirB + ".zip";

        assertTrue(amazonS3.doesObjectExist(bucket, keyDirA));
        assertTrue(amazonS3.doesObjectExist(bucket, keyDirB));
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
        return amazonS3.getObjectAsString(bucket, key);
    }
}
