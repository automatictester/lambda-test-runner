package uk.co.automatictester.lambdatestrunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.lang.ProcessBuilder.Redirect;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ProcessRunner {

    private static final Logger log = LogManager.getLogger(ProcessRunner.class);

    private ProcessRunner() {
    }

    public static int runProcess(List<String> command) {
        File workDir = new File(Config.getProperty("work.dir"));
        return runProcess(command, workDir);
    }

    public static int runProcess(List<String> command, File workDir) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(workDir)
                    .redirectOutput(Redirect.PIPE)
                    .redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8));

            String line = "";
            while ((line = bReader.readLine()) != null) {
                log.info(line);
            }

            return process.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
