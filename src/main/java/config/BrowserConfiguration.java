package config;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.FileDownloadMode;
import constants.Credentials;
import constants.FilePath;

import java.util.Objects;

public class BrowserConfiguration {
    private static final String CHROME_DRIVER_PATH = "/driver/chrome_123.exe";

    public static void configureChromeDriver() {
        System.setProperty("webdriver.chrome.driver", Objects.requireNonNull(
                BrowserConfiguration.class.getResource(CHROME_DRIVER_PATH)).getFile());
        Configuration.browser = "chrome";
        Configuration.browserVersion = "123";
        Configuration.browserSize = "1920x1080";
        Configuration.holdBrowserOpen = false;
        Configuration.screenshots = true;
        Configuration.timeout = 300 * 1000;
        Configuration.downloadsFolder = FilePath.DOWNLOAD_FOLDER.getValue();
        Configuration.baseUrl = Credentials.getInstance().webUrl();
        Configuration.fileDownload = FileDownloadMode.PROXY;
        Configuration.reportsFolder = "target/temp/screenshots";
        Configuration.proxyEnabled = true;
        Configuration.headless = true;
        Configuration.browserBinary = "C:\\Users\\bell\\ChromeTest\\chrome-win64\\chrome.exe";
    }

}
