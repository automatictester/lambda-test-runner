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

    private boolean jdkInstalled = false;

    private static final Logger log = LogManager.getLogger(Handler.class);

    // TODO: general cleanup
    // TODO: extract /tmp and subdirs from all classes
    // TODO: log free space
    // TODO: optional /tmp/.m2 cleanup
    // TODO: make JDK version configurable
    // TODO: test concurrency
    // TODO: /bin/bash
    // TODO: add jacoco
    // TODO: end-to-end test
    // TODO: add travis for building PRs
    // TODO: IAM role definition
    // TODO: store results in S3
    // TODO: always verify exit code from ProcessRunner
    @Override
    public Response handleRequest(Request request, Context context) {
        installJdkOnLambda(context);

        File targetDir = new File(request.getTargetDir());
        try {
            FileUtils.deleteDirectory(targetDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String repoUri = request.getRepoUri();
        GitCloner.cloneRepo(repoUri, targetDir);

        List<String> command = transformCommand(request.getCommand());
        ProcessRunner.runProcess(command, targetDir);

        return null;
    }

    private void installJdkOnLambda(Context context) {
        if (context != null) installJdk();
    }

    private void installJdk() {
        if (jdkInstalled) {
            log.info("JDK already installed, skipping");
        } else {
            log.info("Installing JDK...");

            File dir = new File("/tmp");
            List<String> rm = transformCommand("rm -rf /tmp/jdk10");
            ProcessRunner.runProcess(rm, dir);

            List<String> curl = new ArrayList<>();
            curl.add("/bin/sh");
            curl.add("-c");
            curl.add("curl https://download.java.net/java/GA/jdk10/10.0.2/19aef61b38124481863b1413dce1855f/13/openjdk-10.0.2_linux-x64_bin.tar.gz | gunzip -c | tar xf - -C /tmp");
            ProcessRunner.runProcess(curl, dir);

            List<String> mv = transformCommand("mv /tmp/jdk-10.0.2 /tmp/jdk10");
            ProcessRunner.runProcess(mv, dir);

            jdkInstalled = true;
        }
    }

    private List<String> transformCommand(String command) {
        return Arrays.asList(command.split(" "));
    }
}
