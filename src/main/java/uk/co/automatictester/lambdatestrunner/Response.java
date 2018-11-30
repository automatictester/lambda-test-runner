package uk.co.automatictester.lambdatestrunner;

public class Response {

    private String output;
    private int exitCode;

    public String getOutput() {
        return output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
}
