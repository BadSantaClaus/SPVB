package config;

import FlaNium.WinAPI.webdriver.DesktopOptions;
import FlaNium.WinAPI.webdriver.FlaNiumDriver;
import FlaNium.WinAPI.webdriver.FlaNiumDriverService;
import constants.CmdCommand;
import constants.FilePath;
import lombok.SneakyThrows;
import utils.SpvbUtils;

import java.io.File;
import java.time.Duration;

public class DesktopConfiguration {

    public static FlaNiumDriver driver;

    public static DesktopOptions options = new DesktopOptions()
            .setConnectToRunningApp(false)
            .setLaunchDelay(5000)
            .setResponseTimeout(50000);

    public static void configureDesktopDriver(String appPath) {
        FlaNiumDriverService service = new FlaNiumDriverService.Builder()
                .usingDriverExecutable(new File(FilePath.FLANIUM.getValue()).getAbsoluteFile())
                .withVerbose(false)
                .withSilent(true)
                .withTimeout(Duration.ofSeconds(20))
                .build();
        options.setApplicationPath(new File(appPath).getAbsolutePath());
        driver = new FlaNiumDriver(service, options);
        Runtime.getRuntime().addShutdownHook(new Thread(DesktopConfiguration::close));
    }

    @SneakyThrows
    public static void close() {
        Runtime.getRuntime().exec(CmdCommand.CLOSE_QUIK.getValue());
        SpvbUtils.cleanQuikFiles();
    }

}
