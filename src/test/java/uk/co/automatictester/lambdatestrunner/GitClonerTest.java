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

public class GitClonerTest {

    private File workDir = new File(Config.getProperty("repo.dir"));

    @BeforeMethod
    public void deleteDir() throws IOException {
        FileUtils.deleteDirectory(workDir);
    }

    @Test
    public void testCloneRepo() {
        String repoUri = "https://github.com/automatictester/lambda-test-runner.git";
        String branch = "master";
        GitCloner.cloneRepo(repoUri, branch, workDir);

        String readmeFile = workDir.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }

    @Test
    public void testCloneRepoCheckoutNonDefaultBranch() {
        String repoUri = "https://github.com/automatictester/lambda-test-runner.git";
        String branch = "unit-testing";
        GitCloner.cloneRepo(repoUri, branch, workDir);

        String readmeFile = workDir.toString() + "/src/test/java/uk/co/automatictester/lambdatestrunner/YetAnotherSmokeTest.java";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }
}
