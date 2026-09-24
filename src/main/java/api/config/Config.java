package api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Config INSTANCE = new Config();
    private final Properties properties = new Properties();


    private Config() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("config.properties not found in properties");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("config.properties not load");
        }
    }

    public static String getProperty(String key) {
        String systemValue = System.getProperty(key);

        if (systemValue != null) {
            return systemValue;
        }

        String envValue = System.getenv(key);

        if (envValue != null) {
            return envValue;
        }

        envValue = System.getenv(key.toUpperCase());
        if (envValue != null) {
            return envValue;
        }

        return INSTANCE.properties.getProperty(key);
    }

}
