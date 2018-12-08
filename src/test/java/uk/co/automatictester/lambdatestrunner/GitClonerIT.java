package uk.co.automatictester.lambdatestrunner;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertTrue;

public class GitClonerIT {

    private static final File REPO_DIR = new File(System.getenv("REPO_DIR"));

    @BeforeMethod
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(REPO_DIR);
    }

    @Test
    public void testCloneRepoGitHubOverSsh() {
        String repoUri = "git@github.com:automatictester/lambda-test-runner.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test
    public void testCloneRepoBitBucketOverSsh() {
        String repoUri = "git@bitbucket.org:buildlogic/sample-private-repo.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }
}
