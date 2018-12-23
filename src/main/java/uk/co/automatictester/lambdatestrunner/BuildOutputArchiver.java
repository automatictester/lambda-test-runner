package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeroturnaround.zip.ZipUtil;

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
        if (dirs == null) throw new IllegalArgumentException("Parameter 'dirs' cannot be null");
        for (String dir : dirs) {
            store(dir, commonS3Prefix);
        }
    }

    private void store(String dir, String commonS3Prefix) {
        Path absoluteDirPath = Paths.get(workDir + "/" + dir);
        if (Files.exists(absoluteDirPath) && Files.isDirectory(absoluteDirPath) && Files.isReadable(absoluteDirPath)) {
            String absoluteZipFile = absoluteDirPath + ".zip";
            compress(absoluteDirPath.toString(), absoluteZipFile);
            String relativeZipFile = dir + ".zip";
            upload(commonS3Prefix, relativeZipFile);
        } else {
            log.warn("Dir '{}' does not exist, is not readable or is not a directory", absoluteDirPath.toAbsolutePath());
        }
    }

    private void compress(String absoluteDir, String absoluteZipFile) {
        log.info("Compressing '{}' to '{}'", absoluteDir, absoluteZipFile);
        ZipUtil.pack(new File(absoluteDir), new File(absoluteZipFile));
    }

    private void upload(String commonS3Prefix, String relativeZipFile) {
        AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
        String s3Key = commonS3Prefix + "/" + relativeZipFile;
        String destination = s3Bucket + "/" + s3Key;
        String absoluteZipFile = workDir + "/" + relativeZipFile;
        log.info("Uploading '{}' to '{}'", absoluteZipFile, destination);
        amazonS3.putObject(s3Bucket, s3Key, new File(absoluteZipFile));
        log.info("Upload finished");
    }
}
