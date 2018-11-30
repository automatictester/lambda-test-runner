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
            }
        }
    }

    private void deleteRepoDir() {
        File workDir = new File(Config.getProperty("repo.dir"));
        try {
            FileUtils.deleteDirectory(workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cloneRepo(Request request) {
        String repoUri = request.getRepoUri();
        File repoDir = new File(Config.getProperty("repo.dir"));
        GitCloner.cloneRepo(repoUri, repoDir);
    }

    private ProcessResult runCommand(Request request) {
        List<String> command = transformCommand(request.getCommand());
        File dir = new File(Config.getProperty("repo.dir"));
        return ProcessRunner.runProcess(command, dir);
    }

    private Response createResponse(ProcessResult processResult) {
        Response response = new Response();
        response.setOutput(processResult.getOutput());
        return response;
    }

    private List<String> transformCommand(String command) {
        return Arrays.asList(command.split(" "));
    }
}
