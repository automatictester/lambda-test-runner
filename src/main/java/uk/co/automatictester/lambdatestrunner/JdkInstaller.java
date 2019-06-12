package uk.co.automatictester.lambdatestrunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.automatictester.lambdatestrunner.process.ProcessResult;
import uk.co.automatictester.lambdatestrunner.process.ProcessRunner;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JdkInstaller {

    private static final Logger log = LogManager.getLogger(JdkInstaller.class);

    private JdkInstaller() {
    }

    public static ProcessResult installJdk(Map<String, String> extraEnvVars) {
        log.info("Installing JDK...");
        Instant start = Instant.now();

        File tempDir = new File(System.getenv("TEMP_DIR"));
        String logFile = "jdk-installation.log";
        ProcessResult jdkInstallationResult = ProcessRunner.runProcess(getCommand(), tempDir, extraEnvVars, logFile);

        if (jdkInstallationResult.getExitCode() != 0) {
            return jdkInstallationResult;
        } else {
            Instant finish = Instant.now();
            Duration duration = Duration.between(start, finish);
            log.info("JDK installation complete, took {} s", duration.getSeconds());
            return jdkInstallationResult;
        }
    }

    private static List<String> getCommand() {
        List<String> shellCommand = new ArrayList<>();
        shellCommand.add("/bin/bash");
        shellCommand.add("-c");
        String downloadUrl = JdkVersionSelector.getDownloadUrl();
        String downloadAndExtractShellCommend = String.format("rm -rf /tmp/jdk; mkdir -p /tmp/jdk; curl %s | gunzip -c | tar xf - -C /tmp/jdk --strip-components=1", downloadUrl);
        shellCommand.add(downloadAndExtractShellCommend);
        return shellCommand;
    }

    static class JdkVersionSelector {

        private JdkVersionSelector() {
        }

        public static String getDownloadUrl() {
            String defaultDownloadUrl = "https://download.java.net/java/GA/jdk10/10.0.2/19aef61b38124481863b1413dce1855f/13/openjdk-10.0.2_linux-x64_bin.tar.gz";
            String javaVersion = System.getenv("JAVA_VERSION");
            if (javaVersion == null) {
                return defaultDownloadUrl;
            }
            switch (javaVersion) {
                case "9.0.4":
                    return "https://download.java.net/java/GA/jdk9/9.0.4/binaries/openjdk-9.0.4_linux-x64_bin.tar.gz";
                case "11.0.2":
                    return "https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_linux-x64_bin.tar.gz";
                case "12.0.1":
                    return "https://download.java.net/java/GA/jdk12.0.1/69cfe15208a647278a19ef0990eea691/12/GPL/openjdk-12.0.1_linux-x64_bin.tar.gz";
                case "10.0.2":
                default:
                    return defaultDownloadUrl;
            }
        }
    }
}
