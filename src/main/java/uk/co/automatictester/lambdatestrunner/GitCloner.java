package uk.co.automatictester.lambdatestrunner;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class GitCloner {

    private static final Logger log = LogManager.getLogger(GitCloner.class);

    private GitCloner() {}

    public static void deleteRepoDir() {
        File repoDir = new File(System.getProperty("REPO_DIR"));
        try {
            log.info("Deleting {}", repoDir);
            FileUtils.deleteDirectory(repoDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cloneRepo(String repoUri, String branch, File dir) {
        log.info("Git repo '{}', branch '{}', dir '{}'", repoUri, branch, dir);
        Instant start = Instant.now();
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(repoUri)
                .setBranch(branch)
                .setDirectory(dir);
        execute(cloneCommand);
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        log.info("Cloning took {}s", duration.getSeconds());
    }

    private static void execute(CloneCommand cloneCommand) {
        try {
            cloneCommand.call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
