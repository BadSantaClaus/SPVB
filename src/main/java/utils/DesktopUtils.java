package utils;

import FlaNium.WinAPI.DesktopElement;
import FlaNium.WinAPI.enums.BasePoint;
import config.DesktopConfiguration;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import page.quik.BaseQuikPage;

import java.io.ByteArrayInputStream;
import java.time.Duration;

import static org.openqa.selenium.Keys.*;

@Slf4j
@SuppressWarnings({"UnusedReturnValue"})
public class DesktopUtils {

    private static final int delay = 500;

    public static WebElement findByAutomationId(String automationId) {
        WaitingUtils.waitReady(String.format("//*[(@AutomationId = '%s')]", automationId),
                () -> DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@AutomationId = '%s')]", automationId))));
        return DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@AutomationId = '%s')]", automationId)));
    }

    public static WebElement findByAutomationId(String automationId, String extraCondition) {
        WaitingUtils.waitReady(String.format("//*[(@AutomationId = '%s') and %s]", automationId, extraCondition),
                () -> DesktopConfiguration.driver.findElement(By.xpath((String.format("//*[(@AutomationId = '%s') and %s]", automationId, extraCondition)))));
        return DesktopConfiguration.driver.findElement(By.xpath((String.format("//*[(@AutomationId = '%s') and %s]", automationId, extraCondition))));
    }

    public static WebElement findByName(String name) {
        WaitingUtils.waitReady(String.format("//*[(@Name = '%s')]", name),
                () -> DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@Name = '%s')]", name))));
        return DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@Name = '%s')]", name)));
    }

    public static WebElement findByName(String name, String extraCondition) {
        WaitingUtils.waitReady(String.format("//*[(@Name = '%s') and %s]", name, extraCondition),
                () -> DesktopConfiguration.driver.findElement(By.xpath((String.format("//*[(@AutomationId = '%s') and %s]", name, extraCondition)))));
        return DesktopConfiguration.driver.findElement(By.xpath((String.format("//*[(@AutomationId = '%s') and %s]", name, extraCondition))));
    }

    public static WebElement findByNameContains(String name) {
        WaitingUtils.waitReady(String.format("//*[(@Name = '%s')]", name),
                () -> DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[contains(@Name, '%s')]", name))));
        return DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[contains(@Name, '%s')]", name)));
    }
    public static WebElement findByClassName(String className) {
        WaitingUtils.waitReady(String.format("//*[(@ClassName = '%s')]", className),
                () -> DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@ClassName = '%s')]", className))));
        return DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@ClassName = '%s')]", className)));
    }

    public static WebElement findByControlType(String controlType) {
        WaitingUtils.waitReady(String.format("//*[(@ControlType = '%s')]", controlType),
                () -> DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@ControlType = '%s')]", controlType))));
        return DesktopConfiguration.driver.findElement(By.xpath(String.format("//*[(@ControlType  = '%s')]", controlType)));
    }


    @SneakyThrows
    public static void maximizeWindow() {
        getPressAction(ALT, SPACE).perform();
        pressKey(DOWN);
        pressKey(DOWN);
        pressKey(DOWN);
        pressKey(DOWN);
        pressKey(ENTER);
    }

    public static Action getPressAction(CharSequence key) {
        return new Actions(DesktopConfiguration.driver)
                .keyDown(key).keyUp(key)
                .pause(Duration.ofMillis(delay))
                .build();
    }

    public static Action getPressAction(Keys key, CharSequence sendKey) {
        return new Actions(DesktopConfiguration.driver)
                .keyDown(key)
                .sendKeys(sendKey)
                .keyUp(key)
                .pause(Duration.ofMillis(delay))
                .build();
    }

    public static void showMessageWindow() {
        WebElement el = findByAutomationId("54");
        new DesktopElement(el).mouseActions().mouseRightClick(BasePoint.CENTER, 0, 0);
        WaitingUtils.sleep(1);
        pressKey(DOWN);
        pressKey(DOWN);
        pressKey(DOWN);
        pressKey(ENTER);
    }


    public static void showSearchInstrumentsButton(){
        for (int i = 0; i < 5; i++) {
            try{
                showSearchInstBut();
                break;
            }catch (ConditionTimeoutException | AssertionError e){
                if(i>3)
                    throw e;
            }
        }
    }

    private static void showSearchInstBut(){
        try{
            new BaseQuikPage().searchInstrumentsButton();
        }catch (ConditionTimeoutException | AssertionError e) {
            WebElement el = findByAutomationId("54");
            new DesktopElement(el).mouseActions().mouseRightClick(BasePoint.CENTER, 0, 0);
            for (int i = 0; i < 7; i++) {
                pressKey(DOWN);
            }
            pressKey(ENTER);
        }
        new BaseQuikPage().searchInstrumentsButton();
    }

    public static void pressKey(CharSequence key) {
        getPressAction(key).perform();
    }

    @SneakyThrows
    @Step("screenshot")
    @Attachment(value = "desktop screenshot", type = "image/png")
    public static byte[] takeScreenshot() {
        return ((TakesScreenshot) DesktopConfiguration.driver).getScreenshotAs(OutputType.BYTES);
    }

    @SneakyThrows
    public static void takeNameScreenshot(String name) {
        byte[] screenshot = ((TakesScreenshot) DesktopConfiguration.driver).getScreenshotAs(OutputType.BYTES);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(screenshot)) {
            Allure.addAttachment(name, bis);
        }
    }

    public static void openSearchInstrument() {
        SpvbUtils.step("Открыть окно поиска инструментов");
        showSearchInstrumentsButton();
        new BaseQuikPage().searchInstrumentsButton()
                .click();
    }

}

