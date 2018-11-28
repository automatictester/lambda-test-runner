package uk.co.automatictester.lambdatestrunner;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class GitCloner {

    private GitCloner() {
    }

    public static void cloneRepo(String repoUri, File dir) {
        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(repoUri)
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
