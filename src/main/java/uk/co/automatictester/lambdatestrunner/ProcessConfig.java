package uk.co.automatictester.lambdatestrunner;

import java.io.File;

public class ProcessConfig {

    public File getWorkDir() {
        return new File(Config.getProperty("work.dir"));
    }
}
