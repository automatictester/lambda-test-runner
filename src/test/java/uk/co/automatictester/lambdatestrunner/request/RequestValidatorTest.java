package uk.co.automatictester.lambdatestrunner.request;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestValidatorTest {

    private Request request;

    @BeforeMethod
    public void createRequest() {
        List<String> dirsToStore = new ArrayList<>();
        dirsToStore.add("target/surefire-reports");
        dirsToStore.add("target/failsafe-reports");

        request = new Request();
        request.setRepoUri("https://github.com/automatictester/lambda-test-runner.git");
        request.setBranch("master");
        request.setCommand("./mvnw clean test -Dtest=SmokeTest -Dmaven.repo.local=${MAVEN_USER_HOME}");
        request.setStoreToS3(dirsToStore);
    }

    @Test(groups = "local")
    public void testHappyPath() {
        RequestValidator.validate(request);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testRepoUriNull() {
        request.setRepoUri(null);
        RequestValidator.validate(request);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testRepoUriEmptyString() {
        request.setRepoUri("");
        RequestValidator.validate(request);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testCommandNull() {
        request.setCommand(null);
        RequestValidator.validate(request);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testCommandEmptyString() {
        request.setCommand("");
        RequestValidator.validate(request);
    }

    @Test(groups = "local")
    public void testStoreToS3Null() {
        request.setStoreToS3(null);
        RequestValidator.validate(request);
    }

    @Test(groups = "local")
    public void testStoreToS3EmptyList() {
        request.setStoreToS3(Collections.emptyList());
        RequestValidator.validate(request);
    }

    @Test(groups = "local", expectedExceptions = IllegalArgumentException.class)
    public void testStoreToS3InvalidElement() {
        request.setStoreToS3(Collections.singletonList("/tmp/repo/target/failsafe-reports"));
        RequestValidator.validate(request);
    }
}
