package uk.co.automatictester.lambdatestrunner;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;

public class GitCloner {

    private GitCloner() {}

    // TODO: checkout branch
    // TODO: ssh
    // TODO: https
    public static void cloneRepo(String repoUri, File dir) {
        try {
            Git.cloneRepository()
                    .setURI(repoUri)
                    .setDirectory(dir)
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
