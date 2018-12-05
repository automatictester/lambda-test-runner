package uk.co.automatictester.lambdatestrunner;

public class ProcessResult {

    private int exitCode;
    private String output;

    public String getOutput() {
        return output;
    }

    public String getOutput(int maxOutputSize) {
        if (output.length() > maxOutputSize) {
            return output.substring(output.length() - maxOutputSize);
        } else {
            return output;
        }
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
