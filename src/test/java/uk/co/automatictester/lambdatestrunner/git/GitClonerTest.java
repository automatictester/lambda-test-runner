package uk.co.automatictester.lambdatestrunner.git;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertTrue;

public class GitClonerTest {

    private static final File REPO_DIR = new File(System.getenv("REPO_DIR"));

    @BeforeMethod(alwaysRun = true)
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(REPO_DIR);
    }

    @Test(groups = "local")
    public void testCloneRepoGitHubOverHttps() {
        String repoUri = "https://github.com/automatictester/lambda-test-runner.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test(groups = "local")
    public void testCloneRepoBitBucketOverHttps() {
        String repoUri = "https://buildlogic@bitbucket.org/buildlogic/sample-public-repo.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test(groups = "local")
    public void testCloneRepoGitLabOverHttps() {
        String repoUri = "https://gitlab.com/buildlogic/sample-public-repo.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test(groups = "local")
    public void testCloneRepoWithDefaultDevelopBranchWithoutSpecifyingBranchName() {
        String repoUri = "https://github.com/automatictester/sample-repo-with-develop-branch.git";
        GitCloner.cloneRepo(repoUri, "HEAD", REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test(groups = "local")
    public void testCloneRepoGitHubOverHttpsCheckoutNonDefaultBranch() {
        String repoUri = "https://github.com/automatictester/lambda-test-runner.git";
        String branch = "unit-testing";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String branchSpecificUnitTest = REPO_DIR.toString() + "/src/test/java/uk/co/automatictester/lambdatestrunner/YetAnotherSmokeTest.java";
        Path path = Paths.get(branchSpecificUnitTest);
        assertTrue(Files.exists(path));
    }

    @Test(groups = "local", expectedExceptions = RuntimeException.class)
    public void testCloneRepoNonexistent() {
        String repoUri = "https://github.com/automatictester/lambda-test-runner-2.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);
    }

    @Test(groups = "jenkins")
    public void testCloneRepoGitHubOverSsh() {
        String repoUri = "git@github.com:automatictester/lambda-test-runner.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test(groups = "jenkins")
    public void testCloneRepoBitBucketOverSsh() {
        String repoUri = "git@bitbucket.org:buildlogic/sample-private-repo.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test(groups = "jenkins")
    public void testCloneRepoGitLabOverSsh() {
        String repoUri = "git@gitlab.com:buildlogic/sample-private-repo.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, REPO_DIR);

        String readmeFile = REPO_DIR.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }
}
