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

    private GitCloner() {
    }

    public static void cloneRepo(String repoUri, String branch, File dir) {
        maybeDownloadSshKey(repoUri);
        log.info("Git repo '{}', branch '{}', dir '{}'", repoUri, branch, dir);
        Instant start = Instant.now();
        CloneCommand cloneCommand = CloneCommandFactory.getInstance(repoUri, branch, dir);
        execute(cloneCommand);
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        log.info("Cloning took {} s", duration.getSeconds());
        maybeDeleteSshKey(repoUri);
    }

    private static void maybeDownloadSshKey(String repoUri) {
        if (repoUri.startsWith("git")) {
            SshKeyManager.downloadSshKey();
        }
    }

    private static void maybeDeleteSshKey(String repoUri) {
        if (repoUri.startsWith("git")) {
            SshKeyManager.deleteSshKey();
        }
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
