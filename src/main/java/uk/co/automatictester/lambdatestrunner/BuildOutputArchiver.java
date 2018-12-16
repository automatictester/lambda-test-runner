package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class BuildOutputArchiver {

    private static final Logger log = LogManager.getLogger(BuildOutputArchiver.class);
    private final String s3Bucket = System.getenv("BUILD_OUTPUTS");
    private final String workDir;
    private final String commonS3Prefix;

    public BuildOutputArchiver(String workDir, String commonS3Prefix) {
        this.workDir = workDir;
        this.commonS3Prefix = commonS3Prefix;
    }

    public void store(List<String> dirs) {
        if (dirs == null) throw new IllegalArgumentException("Value cannot be null");
        for (String dir : dirs) {
            store(dir, commonS3Prefix);
        }
    }

    private void store(String dir, String commonS3Prefix) {
        Path dirPath = Paths.get(workDir + "/" + dir);
        if (Files.exists(dirPath) && Files.isDirectory(dirPath) && Files.isReadable(dirPath)) {
            String dirPrefix = commonS3Prefix + "/" + dir;
            String destination = s3Bucket + "/" + dirPrefix;
            log.info("Uploading '{}' to '{}'", dir, destination);
            AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
            TransferManager transferManager = TransferManagerBuilder
                    .standard()
                    .withS3Client(amazonS3)
                    .build();
            try {
                MultipleFileUpload transfer = transferManager.uploadDirectory(s3Bucket, dirPrefix, new File(dirPath.toString()), true);
                transfer.waitForCompletion();
            } catch (AmazonServiceException | InterruptedException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
            log.info("Upload finished");
        } else {
            log.warn("Dir '{}' does not exist, is not readable or is not a directory", dirPath.toAbsolutePath());
        }
    }
}
