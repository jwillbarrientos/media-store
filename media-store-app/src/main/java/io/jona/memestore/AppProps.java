package io.jona.memestore;

import lombok.extern.slf4j.Slf4j;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class AppProps {
    private static final Properties props = new Properties();
    static {
        try (FileInputStream fis = new FileInputStream("app.properties")) {
            props.load(fis);
        } catch (IOException e) {
            log.error("Error while loading app.properties: ", e);
            System.exit(-1 );
        }
    }

    public static String getYtDl() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.equals("win"))
            return props.getProperty("ytDl") + ".exe";
        return props.getProperty("ytDl");
    }

    public static String getTemporalPath() {
        return props.getProperty("videoTemporalPath");
    }

    public static String getVideoOutputPath() {
        return props.getProperty("videoOutputPath");
    }

    public static String getJdbcUrl() {
        return props.getProperty("jdbcUrl");
    }

    public static String getDbUser() {
        return props.getProperty("dbUser");
    }

    public static String getDbPassword() {
        return props.getProperty("dbPassword");
    }
}
