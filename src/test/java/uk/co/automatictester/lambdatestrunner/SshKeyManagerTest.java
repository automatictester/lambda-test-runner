package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import io.findify.s3mock.S3Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SshKeyManagerTest {

    private static final String BUCKET = System.getenv("SSH_KEY_BUCKET");
    private S3Mock s3Mock;
    private final AmazonS3 amazonS3 = AmazonS3Factory.getInstance();

    @BeforeClass(alwaysRun = true)
    public void setupEnv() {
        if (System.getProperty("mockS3") != null) {
            startS3Mock();
            createS3Bucket();
            uploadSshKeyStub();
        }
    }

    private void startS3Mock() {
        int port = 8001;
        s3Mock = new S3Mock.Builder().withPort(port).withInMemoryBackend().build();
        s3Mock.start();
    }

    private void createS3Bucket() {
        amazonS3.createBucket(BUCKET);
    }

    private void uploadSshKeyStub() {
        String s3Key = System.getenv("SSH_KEY_KEY");
        amazonS3.putObject(BUCKET, s3Key, "stub-content");
    }

    @AfterClass(alwaysRun = true)
    public void teardown() {
        if (System.getProperty("mockS3") != null) {
            s3Mock.stop();
        }
    }

    @Test(groups = "local")
    public void testDownloadAndDeleteSshKey() {
        String sshKeyLocal = System.getenv("SSH_KEY_LOCAL");

        SshKeyManager.downloadSshKey();
        assertTrue(Files.exists(Paths.get(sshKeyLocal)));

        SshKeyManager.deleteSshKey();
        assertFalse(Files.exists(Paths.get(sshKeyLocal)));
    }
}
