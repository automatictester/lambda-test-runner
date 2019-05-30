package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class BuildOutputArchiverTest extends AmazonS3Test {

    private final String bucket = System.getenv("BUILD_OUTPUTS");
    private final String workDir = System.getProperty("user.dir");
    private final String commonS3Prefix = getS3Prefix();

    @BeforeClass(alwaysRun = true)
    public void setupEnv() {
        if (System.getProperty("mockS3") != null) {
            if (!amazonS3.doesBucketExistV2(bucket)) {
                amazonS3.createBucket(bucket);
            }
        }
    }

    @Test(groups = "local")
    public void testStoreDirs() {
        String dirNonexistent = "src/test/resources/nonexistent";
        String dirA = "src/test/resources/a";
        String dirB = "src/test/resources/b";

        List<String> dirsToStore = new ArrayList<>();
        dirsToStore.add(dirA);
        dirsToStore.add(dirNonexistent);
        dirsToStore.add(dirB);


        BuildOutputArchiver archiver = new BuildOutputArchiver(workDir, commonS3Prefix);
        archiver.storeDirs(dirsToStore);
        String keyDirA = commonS3Prefix + "/" + dirA + ".zip";
        String keyDirB = commonS3Prefix + "/" + dirB + ".zip";

        assertTrue(amazonS3.doesObjectExist(bucket, keyDirA));
        assertTrue(amazonS3.doesObjectExist(bucket, keyDirB));
    }

    @Test(groups = "local")
    public void testStoreFile() {
        String fileA = "src/test/resources/a/a.txt";
        BuildOutputArchiver archiver = new BuildOutputArchiver(workDir, commonS3Prefix);
        archiver.storeFile(fileA);
        String keyDirA = commonS3Prefix + "/" + fileA;
        assertTrue(amazonS3.doesObjectExist(bucket, keyDirA));
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testStoreNullInput() {
        BuildOutputArchiver archiver = new BuildOutputArchiver(workDir, commonS3Prefix);
        archiver.storeDirs(null);
    }

    private String getS3Prefix() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        return f.format(now);
    }
}
