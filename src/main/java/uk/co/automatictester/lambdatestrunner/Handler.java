package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Handler implements RequestHandler<Request, Response> {

    private static final Logger log = LogManager.getLogger(Handler.class);
    private static final String REPO_DIR = System.getenv("REPO_DIR");
    private boolean jdkInstalled = false;

    @Override
    public Response handleRequest(Request request, Context context) {
        installJdkOnLambda(context);
        cloneRepoToFreshDir(request);
        ProcessResult processResult = runCommand(request);
        return createResponse(processResult);
    }

    private void installJdkOnLambda(Context context) {
        if (context != null) {
            if (jdkInstalled) {
                log.info("JDK already installed, skipping...");
            } else {
                JdkInstaller.installJdk();
                jdkInstalled = true;
            }
        }
    }

    private void cloneRepoToFreshDir(Request request) {
        GitCloner.deleteRepoDir();
        String repoUri = request.getRepoUri();
        String branch = request.getBranch();
        File repoDir = new File(REPO_DIR);
        GitCloner.cloneRepo(repoUri, branch, repoDir);
    }

    private ProcessResult runCommand(Request request) {
        String rawCommand = request.getCommand();
        List<String> command = transformCommand(rawCommand);
        File repoDir = new File(REPO_DIR);
        log.info("Command: {}", rawCommand);
        Instant start = Instant.now();
        ProcessResult processResult = ProcessRunner.runProcess(command, repoDir);
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        log.info("Exit code: {}, command took {}s", processResult.getExitCode(), duration.getSeconds());
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
