package uk.co.automatictester.lambdatestrunner;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.testng.Assert.assertTrue;

public class GitClonerTest {

    @Test
    public void testCloneRepo() throws IOException {
        File targetDir = new File("/tmp/lambda-test-runner/");
        FileUtils.deleteDirectory(targetDir);

        String repoUri = "https://github.com/automatictester/lambda-test-runner.git";
        GitCloner.cloneRepo(repoUri, targetDir);

        String readmeFile = targetDir.toString() + "/README.md";
        Path path = Paths.get(readmeFile);
        assertTrue(Files.exists(path));
    }
}
