package uk.co.automatictester.lambdatestrunner;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProcessRunner {

    private ProcessRunner() {}

    // TODO: log output in real time
    public static void runProcess(List<String> command, File dir, File logFile) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(dir)
                    .redirectErrorStream(true)
                    .redirectOutput(logFile);
            processBuilder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
