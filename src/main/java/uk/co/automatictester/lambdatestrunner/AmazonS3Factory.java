package uk.co.automatictester.lambdatestrunner;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AmazonS3Factory {

    private AmazonS3Factory() {
    }

    public static AmazonS3 getInstance() {
        if (isMockedInstanceExpected()) {
            return getMockedInstance();
        } else {
            return getRealInstance();
        }
    }

    private static boolean isMockedInstanceExpected() {
        return System.getProperty("mockS3") != null;
    }

    private static AmazonS3 getRealInstance() {
        return AmazonS3ClientBuilder.standard().build();
    }

    private static AmazonS3 getMockedInstance() {
        String s3MockUrl = "http://localhost:8001";
        String region = AmazonS3ClientBuilder.standard().getRegion();
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(s3MockUrl, region);
        return AmazonS3ClientBuilder
                .standard()
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpointConfiguration)
                .build();
    }
}
