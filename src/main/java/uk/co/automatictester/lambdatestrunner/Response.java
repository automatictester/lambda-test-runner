package uk.co.automatictester.lambdatestrunner;

public class Response {

    private String output;
    private int exitCode;
    private String s3Prefix;

    public String getOutput() {
        return output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getS3Prefix() {
        return s3Prefix;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix;
    }
}
