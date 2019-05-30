package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.services.s3.AmazonS3;
import io.findify.s3mock.S3Mock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

public abstract class AmazonS3Test {

    protected AmazonS3 amazonS3 = AmazonS3Factory.getInstance();
    protected S3Mock s3Mock;

    private int getMockedPort() {
        return Integer.parseInt(AmazonS3Factory.getMockedPort());
    }

    @BeforeClass(alwaysRun = true)
    public void maybeSetupS3mock() {
        if (System.getProperty("mockS3") != null) {
            s3Mock = new S3Mock.Builder().withPort(getMockedPort()).withInMemoryBackend().build();
            s3Mock.start();
        }
    }

    @AfterClass(alwaysRun = true)
    public void maybeCleanupS3Mock() {
        if (System.getProperty("mockS3") != null) {
            s3Mock.stop();
        }
    }
}
