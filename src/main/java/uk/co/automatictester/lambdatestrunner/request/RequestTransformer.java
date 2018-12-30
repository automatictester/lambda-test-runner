package uk.co.automatictester.lambdatestrunner.request;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RequestTransformer {

    private static final Logger log = LogManager.getLogger(RequestTransformer.class);

    private RequestTransformer() {
    }

    public static Request transform(Request rawRequest) {
        Request request = new Request();
        request.setRepoUri(rawRequest.getRepoUri());
        request.setCommand(rawRequest.getCommand());
        request.setStoreToS3(rawRequest.getStoreToS3());

        if (rawRequest.getBranch() == null || rawRequest.getBranch().equals("")) {
            String branch = "HEAD";
            log.info("Branch not specified, using '{}'", branch);
            request.setBranch(branch);
        } else {
            request.setBranch(rawRequest.getBranch());
        }

        return request;
    }
}
