package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Handler implements RequestHandler<Request, Response> {

    private static final Logger log = LogManager.getLogger(Handler.class);
    private static final String REPO_DIR = System.getenv("REPO_DIR");
    private boolean jdkInstalled = false;

    @Override
    public Response handleRequest(Request request, Context context) {
        logTempDirSize();
        Optional<ProcessResult> jdkInstallationResult = maybeInstallJdkOnLambda(context);
        if (jdkInstallationResult.isPresent() && jdkInstallationResult.get().getExitCode() != 0) {
            log.error("JDK installation unsuccessful, terminating");
            return createResponse(jdkInstallationResult.get());
        }
        maybeDeleteLocalMavenCache();
        maybeDeleteLocalGradleCache();
        cloneRepoToFreshDir(request);
        ProcessResult processResult = runCommand(request);
        logTempDirSize();
        return createResponse(processResult);
    }

    private void logTempDirSize() {
        String temp = System.getenv("TEMP_DIR");
        File tempDir = new File(temp);
        long size = FileUtils.sizeOfDirectory(tempDir);
        long sizeInMb = size / 1024 / 1024;
        log.info("{} dir size: {} MB", temp, sizeInMb);
    }

    private Optional<ProcessResult> maybeInstallJdkOnLambda(Context context) {
        if (context != null) {
            if (jdkInstalled) {
                log.info("JDK already installed, skipping...");
            } else {
                ProcessResult processResult = JdkInstaller.installJdk();
                if (processResult.getExitCode() != 0) {
                    return Optional.of(processResult);
                }
                jdkInstalled = true;
                return Optional.of(processResult);
            }
        }
        return Optional.empty();
    }

    private void maybeDeleteLocalMavenCache() {
        if (System.getenv("M2_CLEANUP").equals("true")) {
            String localMavenCacheDir = System.getenv("MAVEN_USER_HOME");
            deleteDir(localMavenCacheDir);
        }
    }

    private void maybeDeleteLocalGradleCache() {
        if (System.getenv("GRADLE_CLEANUP").equals("true")) {
            String localGradleCacheDir = System.getenv("GRADLE_USER_HOME");
            deleteDir(localGradleCacheDir);
        }
    }

    private void cloneRepoToFreshDir(Request request) {
        deleteDir(REPO_DIR);
        String repoUri = request.getRepoUri();
        String branch = request.getBranch();
        File repoDir = new File(REPO_DIR);
        GitCloner.cloneRepo(repoUri, branch, repoDir);
    }

    private static void deleteDir(String dir) {
        File dirToDelete = new File(dir);
        try {
            log.info("Deleting {}", dirToDelete);
            FileUtils.deleteDirectory(dirToDelete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        log.info("Exit code: {}, command took {} s", processResult.getExitCode(), duration.getSeconds());
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
