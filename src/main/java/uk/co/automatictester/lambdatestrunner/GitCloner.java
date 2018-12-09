package uk.co.automatictester.lambdatestrunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

public class GitCloner {

    private static final Logger log = LogManager.getLogger(GitCloner.class);

    private GitCloner() {}

    public static void cloneRepo(String repoUri, String branch, File dir) {
        log.info("Git repo '{}', branch '{}', dir '{}'", repoUri, branch, dir);
        SshKeyManager.downloadSshKey();
        Instant start = Instant.now();
        CloneCommand cloneCommand = CloneCommandFactory.getInstance(repoUri, branch, dir);
        execute(cloneCommand);
        Instant finish = Instant.now();
        SshKeyManager.deleteSshKey();
        Duration duration = Duration.between(start, finish);
        log.info("Cloning took {} s", duration.getSeconds());
    }

    private static void execute(CloneCommand cloneCommand) {
        try {
            cloneCommand.call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        } catch (JGitInternalException e) {
            log.error("Cloning failed, terminating");
            throw e;
        }
    }
}
