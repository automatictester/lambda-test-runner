package uk.co.automatictester.lambdatestrunner.request;

import java.util.List;

public class RequestValidator {
    private RequestValidator() {
    }

    public static void validate(RawRequest request) {
        validateRepoUri(request.getRepoUri());
        validateCommand(request.getCommand());
        validateStoreToS3(request.getStoreToS3());
    }

    private static void validateRepoUri(String repoUri) {
        if (repoUri == null || repoUri.equals("") ||
                (!repoUri.startsWith("git") && !repoUri.startsWith("http"))) {
            throw new IllegalArgumentException("Invalid repoUri: " + repoUri);
        }
    }

    private static void validateCommand(String command) {
        if (command == null || command.equals("")) {
            throw new IllegalArgumentException("Invalid command: " + command);
        }
    }

    private static void validateStoreToS3(List<String> storeToS3) {
        if (storeToS3 != null) {
            for (String dir : storeToS3) {
                if (dir.startsWith("/")) {
                    throw new IllegalArgumentException("Invalid storeToS3: must be a relative path");
                }
            }
        }
    }
}
