package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Handler implements RequestHandler<Request, Response> {

    private static final Logger log = LogManager.getLogger(Handler.class);
    private boolean jdkInstalled = false;

    @Override
    public Response handleRequest(Request request, Context context) {
        installJdkOnLambda(context);
        deleteRepoDir();
        cloneRepo(request);
        ProcessResult processResult = runCommand(request);
        return createResponse(processResult);
    }

    private void installJdkOnLambda(Context context) {
        if (context != null) {
            if (jdkInstalled) {
                log.info("JDK already installed, skipping...");
            } else {
                log.info("Installing JDK...");
                JdkInstaller.installJdk();
                jdkInstalled = true;
                log.info("JDK installation complete");
            }
        }
    }

    private void deleteRepoDir() {
        String dir = Config.getProperty("repo.dir");
        File workDir = new File(dir);
        try {
            log.info("Deleting {}", dir);
            FileUtils.deleteDirectory(workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cloneRepo(Request request) {
        String repoUri = request.getRepoUri();
        String branch = request.getBranch();
        File repoDir = new File(Config.getProperty("repo.dir"));
        GitCloner.cloneRepo(repoUri, branch, repoDir);
    }

    private ProcessResult runCommand(Request request) {
        String rawCommand = request.getCommand();
        List<String> command = transformCommand(rawCommand);
        File dir = new File(Config.getProperty("repo.dir"));
        log.info("Command: {}", rawCommand);
        ProcessResult processResult = ProcessRunner.runProcess(command, dir);
        log.info("Exit code: {}", processResult.getExitCode());
        return processResult;
    }

    private Response createResponse(ProcessResult processResult) {
        Response response = new Response();
        response.setOutput(processResult.getOutput());
        response.setExitCode(processResult.getExitCode());
        return response;
    }

    private List<String> transformCommand(String command) {
        return Arrays.asList(command.split(" "));
    }
}
