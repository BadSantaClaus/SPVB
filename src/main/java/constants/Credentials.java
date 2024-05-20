package constants;

import config.BrowserConfiguration;
import lombok.SneakyThrows;
import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;

public class Credentials {
    private static ICredentials INSTANCE = ConfigCache.getOrCreate(ICredentials.class, System.getProperties(), System.getenv());

    public static ICredentials getInstance() {
        return INSTANCE;
    }

    @SneakyThrows
    public static void setEnv(String env) {
        ConfigFactory.setProperty("env", env);
        System.setProperty("env", env);
        INSTANCE = ConfigFactory.create(ICredentials.class, System.getProperties(), System.getenv());
        BrowserConfiguration.configureChromeDriver();
    }
}
