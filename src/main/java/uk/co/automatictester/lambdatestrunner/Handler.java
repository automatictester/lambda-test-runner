package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Handler implements RequestHandler<Request, Response> {

    private static final Logger log = LogManager.getLogger(Handler.class);
    private boolean jdkInstalled = false;
    private ProcessConfig processConfig = new ProcessConfig();

    @Override
    public Response handleRequest(Request request, Context context) {
        installJdkOnLambda(context);
        deleteWorkDir();
        cloneRepo(request);
        runCommand(request);
        return new Response();
    }

    private void installJdkOnLambda(Context context) {
        if (context != null) installJdk();
    }

    private void installJdk() {
        if (jdkInstalled) {
            log.info("JDK already installed, skipping");
        } else {
            log.info("Installing JDK...");

//            List<String> rm = transformCommand("rm -rf /tmp/jdk10");
//            new ProcessRunner(processConfig).runProcess(rm);

            List<String> curl = new ArrayList<>();
            curl.add("/bin/sh");
            curl.add("-c");
            curl.add("rm -rf /tmp/jdk10; curl https://download.java.net/java/GA/jdk10/10.0.2/19aef61b38124481863b1413dce1855f/13/openjdk-10.0.2_linux-x64_bin.tar.gz | gunzip -c | tar xf - -C /tmp; mv /tmp/jdk-10.0.2 /tmp/jdk10");
            new ProcessRunner(processConfig).runProcess(curl);

//            List<String> mv = transformCommand("mv /tmp/jdk-10.0.2 /tmp/jdk10");
//            new ProcessRunner(processConfig).runProcess(mv);

            jdkInstalled = true;
        }
    }

    private void deleteWorkDir() {
        File workDir = new File(Config.getProperty("work.dir"));
        try {
            FileUtils.deleteDirectory(workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cloneRepo(Request request) {
        String repoUri = request.getRepoUri();
        File workDir = processConfig.getWorkDir();
        GitCloner.cloneRepo(repoUri, workDir);
    }

    private void runCommand(Request request) {
        List<String> command = transformCommand(request.getCommand());
        new ProcessRunner(processConfig).runProcess(command);
    }

    private List<String> transformCommand(String command) {
        return Arrays.asList(command.split(" "));
    }
}
