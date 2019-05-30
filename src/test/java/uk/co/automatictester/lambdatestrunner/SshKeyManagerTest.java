package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SshKeyManagerTest extends AmazonS3Test {

    private static final String BUCKET = System.getenv("SSH_KEY_BUCKET");

    @BeforeClass(alwaysRun = true)
    public void setupEnv() {
        if (System.getProperty("mockS3") != null) {
            createS3Bucket();
            uploadSshKeyStub();
        }
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
