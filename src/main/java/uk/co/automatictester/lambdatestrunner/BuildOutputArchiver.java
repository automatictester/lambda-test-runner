package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BuildOutputArchiver {

    private static final Logger log = LogManager.getLogger(BuildOutputArchiver.class);
    private static final String S3_BUCKET = System.getenv("BUILD_OUTPUTS");
    private String workDir;

    public BuildOutputArchiver(String workDir) {
        this.workDir = workDir;
    }

    public String store(List<String> dirs) {
        String prefix = getPrefix();
        for (String dir : dirs) {
            store(dir, prefix);
        }
        return prefix;
    }

    private void store(String dir, String commonPrefix) {
        Path dirPath = Paths.get(workDir + "/" + dir);
        if (Files.exists(dirPath) && Files.isDirectory(dirPath) && Files.isReadable(dirPath)) {
            String dirPrefix = commonPrefix + "/" + dir;
            String destination = S3_BUCKET + "/" + dirPrefix;
            log.info("Uploading '{}' to '{}'", dir, destination);
            TransferManager transferManager = TransferManagerBuilder.standard().build();
            try {
                MultipleFileUpload transfer = transferManager.uploadDirectory(S3_BUCKET, dirPrefix, new File(dirPath.toString()), true);
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

    private String getPrefix() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        return f.format(now);
    }
}
