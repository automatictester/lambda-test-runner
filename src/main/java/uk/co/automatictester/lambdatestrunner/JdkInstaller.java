package uk.co.automatictester.lambdatestrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JdkInstaller {

    public static void installJdk() {
        File dir = new File(Config.getProperty("temp.dir"));
        ProcessRunner.runProcess(getCommand(), dir);
    }

    private static List<String> getCommand() {
        List<String> shellCommand = new ArrayList<>();
        shellCommand.add("/bin/bash");
        shellCommand.add("-c");
        shellCommand.add("rm -rf /tmp/jdk10; curl https://download.java.net/java/GA/jdk10/10.0.2/19aef61b38124481863b1413dce1855f/13/openjdk-10.0.2_linux-x64_bin.tar.gz | gunzip -c | tar xf - -C /tmp; mv /tmp/jdk-10.0.2 /tmp/jdk10");
        return shellCommand;
    }
}
