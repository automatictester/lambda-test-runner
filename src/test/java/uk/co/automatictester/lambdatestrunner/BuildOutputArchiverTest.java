package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class BuildOutputArchiverTest {

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
        AmazonS3ClientBuilder amazonS3ClientBuilder = AmazonS3ClientBuilder.standard();
        AmazonS3 client = amazonS3ClientBuilder.build();
        String S3_BUCKET = System.getenv("BUILD_OUTPUTS");
        return client.getObjectAsString(S3_BUCKET, key);
    }
}
