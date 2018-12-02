package uk.co.automatictester.lambdatestrunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JdkInstaller {

    private static final Logger log = LogManager.getLogger(JdkInstaller.class);

    private JdkInstaller() {}

    public static void installJdk() {
        log.info("Installing JDK...");
        Instant start = Instant.now();
        File tempDir = new File(System.getenv("TEMP_DIR"));
        ProcessRunner.runProcess(getCommand(), tempDir);
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        log.info("JDK installation complete, took {}s", duration.getSeconds());
    }

    private static List<String> getCommand() {
        List<String> shellCommand = new ArrayList<>();
        shellCommand.add("/bin/bash");
        shellCommand.add("-c");
        shellCommand.add(getRawCommand());
        return shellCommand;
    }

    private static String getRawCommand() {
        String javaHome = System.getenv("JAVA_HOME");
        String tempDir = System.getenv("TEMP_DIR");
        return String.format("rm -rf %s; curl https://download.java.net/java/GA/jdk10/10.0.2/19aef61b38124481863b1413dce1855f/13/openjdk-10.0.2_linux-x64_bin.tar.gz | gunzip -c | tar xf - -C %s; mv %s/jdk-10.0.2 %s",
                javaHome, tempDir, tempDir, javaHome);
    }
}
