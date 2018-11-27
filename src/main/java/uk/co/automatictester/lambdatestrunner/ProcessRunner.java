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

    // TODO: string += ...
    public static void runProcess(List<String> command, File dir) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(dir)
                    .redirectOutput(Redirect.PIPE)
                    .redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8));

            String output = "";
            String line;
            while ((line = bReader.readLine()) != null) {
//                log.info(line);
                output += line + "\n";
            }

            process.waitFor();
            log.info(output);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
