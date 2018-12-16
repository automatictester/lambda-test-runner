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
        CloneCommand cloneCommand = CloneCommandFactory.getInstance(repoUri, branch, dir);
        try {
            executeCloneCommand(cloneCommand);
        } finally {
            maybeDeleteSshKey(repoUri);
        }
    }

    public static void cloneRepo(String repoUri, File dir) {
        maybeDownloadSshKey(repoUri);
        log.info("Git repo '{}', dir '{}'", repoUri, dir);
        CloneCommand cloneCommand = CloneCommandFactory.getInstance(repoUri, dir);
        try {
            executeCloneCommand(cloneCommand);
        } finally {
            maybeDeleteSshKey(repoUri);
        }
    }

    private static void executeCloneCommand(CloneCommand cloneCommand) {
        Instant start = Instant.now();
        try {
            cloneCommand.call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        } catch (JGitInternalException e) {
            log.error("Cloning failed, terminating");
            throw e;
        }
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        log.info("Cloning took {} s", duration.getSeconds());
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
}
