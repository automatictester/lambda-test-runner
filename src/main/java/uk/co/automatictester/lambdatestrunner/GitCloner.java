package uk.co.automatictester.lambdatestrunner;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

public class GitCloner {

    private static final Logger log = LogManager.getLogger(GitCloner.class);

    private GitCloner() {}

    public static void deleteRepoDir() {
        String dir = Config.getProperty("repo.dir");
        File workDir = new File(dir);
        try {
            log.info("Deleting {}", dir);
            FileUtils.deleteDirectory(workDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void cloneRepo(String repoUri, String branch, File dir) {
        log.info("Git repo '{}', branch '{}', dir '{}'", repoUri, branch, dir);
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(repoUri)
                .setBranch(branch)
                .setDirectory(dir);
        execute(cloneCommand);
    }

    private static void execute(CloneCommand cloneCommand) {
        try {
            cloneCommand.call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
