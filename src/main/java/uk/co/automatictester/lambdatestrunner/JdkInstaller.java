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
        String logFile = System.getenv("JDK_INSTALLATION_LOG");
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
        shellCommand.add("rm -rf /tmp/jdk10; curl https://download.java.net/java/GA/jdk10/10.0.2/19aef61b38124481863b1413dce1855f/13/openjdk-10.0.2_linux-x64_bin.tar.gz | gunzip -c | tar xf - -C /tmp; mv /tmp/jdk-10.0.2 /tmp/jdk10");
        return shellCommand;
    }
}
