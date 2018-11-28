package uk.co.automatictester.lambdatestrunner;

import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Properties properties;

    private Config() {}

    static {
        properties = new Properties();
        try {
            properties.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
