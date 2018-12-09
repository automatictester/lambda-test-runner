package uk.co.automatictester.lambdatestrunner;

import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SshKeyManagerTest {

    @Test(groups = "jenkins")
    public void testDownloadAndDeleteSshKey() {
        String sshKeyLocal = System.getenv("SSH_KEY_LOCAL");

        SshKeyManager.downloadSshKey();
        assertTrue(Files.exists(Paths.get(sshKeyLocal)));

        SshKeyManager.deleteSshKey();
        assertFalse(Files.exists(Paths.get(sshKeyLocal)));
    }
}
