package ru.spvb.auction.common;


import com.codeborne.selenide.Selenide;
import config.BrowserConfiguration;
import config.TestExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import page.quik.RequestPage;
import utils.SpvbUtils;

@ExtendWith(TestExceptionHandler.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BaseTest {

    @BeforeEach
    public void setup() {
        SpvbUtils.cleanQuikFiles();
        SpvbUtils.cleanTempFiles();
        SpvbUtils.createTempFilesDir();
        BrowserConfiguration.configureChromeDriver();
        RequestPage.getInstance().getRequestNumbers().clear();
    }

    @AfterEach
    public void clean() {
        Selenide.closeWebDriver();
    }
}
