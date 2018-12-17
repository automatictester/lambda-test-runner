package uk.co.automatictester.lambdatestrunner.request;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestValidatorTest {

    private RawRequest rawRequest;

    @BeforeMethod(alwaysRun = true)
    public void createRequest() {
        List<String> dirsToStore = new ArrayList<>();
        dirsToStore.add("target/surefire-reports");
        dirsToStore.add("target/failsafe-reports");

        rawRequest = new RawRequest();
        rawRequest.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        rawRequest.setBranch("master");
        rawRequest.setCommand("./mvnw clean test -Dtest=SmokeTest -Dmaven.repo.local=${MAVEN_USER_HOME}");
        rawRequest.setStoreToS3(dirsToStore);
    }

    @Test(groups = "local")
    public void testHappyPath() {
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testRepoUriNull() {
        rawRequest.setRepoUri(null);
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testRepoUriEmptyString() {
        rawRequest.setRepoUri("");
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testRepoUriUnknown() {
        rawRequest.setRepoUri("ftp://github.com/automatictester/lambda-test-runner.git");
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testCommandNull() {
        rawRequest.setCommand(null);
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testCommandEmptyString() {
        rawRequest.setCommand("");
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local")
    public void testStoreToS3Null() {
        rawRequest.setStoreToS3(null);
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local")
    public void testStoreToS3EmptyList() {
        rawRequest.setStoreToS3(Collections.emptyList());
        RequestValidator.validate(rawRequest);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testStoreToS3InvalidElement() {
        rawRequest.setStoreToS3(Collections.singletonList("/tmp/repo/target/failsafe-reports"));
        RequestValidator.validate(rawRequest);
    }
}
