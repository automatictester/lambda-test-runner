package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertNotNull;

public class AmazonS3FactoryTest {

    @AfterMethod
    public void cleanUpVmOptions() {
        System.clearProperty("mockS3");
        System.clearProperty("mockS3Port");
    }

    @Test
    public void testGetRealInstance() {
        AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
        assertNotNull(amazonS3);
    }

    @Test
    public void testGetMockedInstance() {
        System.setProperty("mockS3", "true");
        AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
        assertNotNull(amazonS3);
    }

    @Test
    public void testGetStandardMockPort() {
        System.setProperty("mockS3", "true");

        String mockedPort = AmazonS3Factory.getMockedPort();

        assertThat(mockedPort, is(equalTo("8001")));
    }

    @Test
    public void testGetNonStandardMockPort() {
        System.setProperty("mockS3", "true");
        System.setProperty("mockS3Port", "8002");

        String mockedPort = AmazonS3Factory.getMockedPort();

        assertThat(mockedPort, is(equalTo("8002")));
    }

    @Test
    public void testGetMockedUrlStandardPort() {
        System.setProperty("mockS3", "true");
        String standardMockedUrl = "http://localhost:8001";

        String mockedUrl = AmazonS3Factory.getMockedUrl();

        assertThat(mockedUrl, is(equalTo(standardMockedUrl)));
    }

    @Test
    public void testGetMockedUrlNonStandardPort() {
        String nonStandardPort = "8002";
        String nonStandardMockedUrl = String.format("http://localhost:%s", nonStandardPort);
        System.setProperty("mockS3", "true");
        System.setProperty("mockS3Port", nonStandardPort);

        String mockedUrl = AmazonS3Factory.getMockedUrl();

        assertThat(mockedUrl, is(equalTo(nonStandardMockedUrl)));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetMockedUrlWithoutMockingEnabled() {
        AmazonS3Factory.getMockedUrl();
    }

}