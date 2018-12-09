package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SshKeyManager {

    private static final Logger log = LogManager.getLogger(SshKeyManager.class);
    private static final String SSH_KEY_LOCAL = System.getenv("SSH_KEY_LOCAL");

    private SshKeyManager() {
    }

    public static void downloadSshKey() {
        log.debug("Downloading SSH key");
        String content = getSshKeyFromS3();
        storeSshKeyToDisk(content);
    }

    public static void deleteSshKey() {
        log.debug("Deleting downloaded SSH key");
        try {
            Files.delete(Paths.get(SSH_KEY_LOCAL));
        } catch (IOException e) {
            log.error("Error deleting SSH key '{}'", SSH_KEY_LOCAL);
            throw new RuntimeException(e);
        }
    }

    private static String getSshKeyFromS3() {
        AmazonS3 amazonS3 = AmazonS3Factory.getRealInstance();
        String bucket = System.getenv("SSH_KEY_BUCKET");
        String key = System.getenv("SSH_KEY_KEY");
        return amazonS3.getObjectAsString(bucket, key);
    }

    private static void storeSshKeyToDisk(String content) {
        try {
            Files.write(Paths.get(SSH_KEY_LOCAL), content.getBytes());
        } catch (IOException e) {
            log.error("Error storing SSH key to disk '{}'", SSH_KEY_LOCAL);
            throw new RuntimeException(e);
        }
    }
}
