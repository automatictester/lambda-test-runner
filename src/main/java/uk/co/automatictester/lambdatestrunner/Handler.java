package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Handler implements RequestHandler<Request, Response> {

    private static final Logger log = LogManager.getLogger(Handler.class);

    // TODO: cleanup
    // TODO: installJdk once
    // TODO: extract /tmp and subdirs from all classes
    @Override
    public Response handleRequest(Request request, Context context) {
        File targetDir = new File(request.getTargetDir());
        try {
            FileUtils.deleteDirectory(targetDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String repoUri = request.getRepoUri();
        GitCloner.cloneRepo(repoUri, targetDir);
        installJdk();

        List<String> command = transformCommand(request.getCommand());
        String log = request.getLogFile();
        File logFile = new File(log);
        ProcessRunner.runProcess(command, targetDir, logFile);

        logOutput(log);
        return null;
    }

    private void installJdk() {
        List<String> command = transformCommand("(cd /tmp; rm -rf jdk11; curl https://download.java.net/java/GA/jdk11/13/GPL/openjdk-11.0.1_linux-x64_bin.tar.gz | gunzip -c | tar xf -; mv jdk-11.0.1 jdk11)");
        File dir = new File("/tmp/lambda-test-runner/scripts");
        File logFile = new File("/tmp/lambda-test-runner/scripts/jdk-installer.log");
        ProcessRunner.runProcess(command, dir, logFile);
        logOutput(logFile.toString());
    }

    private List<String> transformCommand(String command) {
        return Arrays.asList(command.split(" "));
    }

    private void logOutput(String logFile) {
        Path path = Paths.get(logFile);
        Stream<String> output;
        try {
            output = Files.lines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        output.forEach(System.out::println);
    }
}
