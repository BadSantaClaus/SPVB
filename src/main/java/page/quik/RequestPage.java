package page.quik;

import FlaNium.WinAPI.elements.TextBox;
import FlaNium.WinAPI.enums.BasePoint;
import config.DesktopConfiguration;
import io.qameta.allure.Step;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import utils.DesktopUtils;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static elements.Components.desktopTextBox;
import static org.openqa.selenium.Keys.*;
import static utils.DesktopUtils.findByAutomationId;

public class RequestPage extends BaseQuikPage {

    public TextBox percentBox() {
        return desktopTextBox(DesktopUtils.findByAutomationId("10930"), "Ставка %");
    }

    public TextBox price() {
        return desktopTextBox(DesktopUtils.findByAutomationId("9947"), "Цена");
    }

    public TextBox quantity() {
        return desktopTextBox(DesktopUtils.findByAutomationId("9948"), "Количество лотов");
    }

    public TextBox clientCode() {
        return desktopTextBox(DesktopUtils.findByAutomationId("9522"), "Код клиента");
    }

    private final String TEMP_FILE_PATH = "target\\temp\\request_table.txt";

    @Getter
    private final List<String> requestNumbers = new ArrayList<>();

    public static RequestPage instance;

    public static RequestPage getInstance() {
        if (instance == null) {
            instance = new RequestPage();
        }
        return instance;
    }

    @Step("Создать заявку на покупку в Quik по цене {price} в количестве {quantity}, ")
    public void makeRequest(String price, String quantity, String clientCode) {
        new Actions(DesktopConfiguration.driver).keyDown(Keys.F2).keyUp(Keys.F2).perform();
        price().click();
        new Actions(DesktopConfiguration.driver).sendKeys(price).perform();
        SpvbUtils.step(String.format("Ввести значение %s в поле цена", price));
        quantity().click();
        new Actions(DesktopConfiguration.driver).sendKeys(quantity).perform();
        SpvbUtils.step(String.format("Ввести значение %s в поле Количество лотов", quantity));
        clientCode().setText(clientCode);
        DesktopUtils.takeScreenshot();
        okButton().click();
        okButton().click();
        WaitingUtils.sleep(1);
        checkLastDialogueMessageContains("зарегистрирована", "Проверить, что заявка зарегистрирована");
        addRequestNumber();
    }

    @Step("Создать заявку на покупку в Quik")
    public RequestPage makeRequest(String clientCode) {
        DesktopUtils.pressKey(Keys.F2);
        clientCode().setText(clientCode);
        okButton().click();
        okButton().click();
        checkLastDialogueMessageContains("зарегистрирована", "Проверить, что заявка зарегистрирована");
        addRequestNumber();
        return this;
    }

    @Step("Создать заявку на покупку в Quik")
    public void makeRequest(String percent, String quantity) {
        DesktopUtils.pressKey(Keys.F2);
        percentBox().click();
        new Actions(DesktopConfiguration.driver).sendKeys(percent).perform();
        quantity().click();
        new Actions(DesktopConfiguration.driver).sendKeys(quantity).perform();
        DesktopUtils.takeScreenshot();
        okButton().click();
        okButton().click();
        checkLastDialogueMessageContains("зарегистрирована", "Проверить, что заявка зарегистрирована");
        addRequestNumber();
    }

    @Step("Измненить ставку")
    public void setPercentBox(String percent) {
        WaitingUtils.sleep(1);
        new Actions(DesktopConfiguration.driver).keyDown(F8).keyUp(F8).perform();
        int i = 0;
        while (i < 5) {
            try {
                percentBox().click();
                break;
            } catch (Exception | Error e) {
                new Actions(DesktopConfiguration.driver).keyDown(F8).keyUp(F8).perform();
                i++;
            }
        }
        new Actions(DesktopConfiguration.driver).sendKeys(percent).perform();
        okButton().click();
        okButton().click();
        checkLastDialogueMessageContains("заменена", "Проверить, что заявка зарегистрирована");
    }

    @Step("Измненить количество")
    public void setQuantityBox(String quantity) {
        new Actions(DesktopConfiguration.driver).keyDown(F2).keyUp(F2).perform();
        quantity().click();
        new Actions(DesktopConfiguration.driver).sendKeys(quantity).perform();
        okButton().click();
        okButton().click();
        checkLastDialogueMessageContains("изменена", "Проверить, что заявка зарегистрирована");
    }

    public RequestPage saveToFile() {
        deleteSavedData();
        currentTrades().mouseActions().mouseRightClick(BasePoint.CENTER_TOP, 300, 500);
        WaitingUtils.sleep(1);
        for (int i = 0; i < 9; i++) {
            DesktopUtils.pressKey(Keys.UP);
        }
        DesktopUtils.pressKey(Keys.ENTER);
        new TextBox(findByAutomationId("1001", "(@ControlType = 'Edit')")).setText(System.getProperty("user.dir") + "\\" + TEMP_FILE_PATH);
        okButton().click();
        WaitingUtils.sleep(1);
        return this;
    }

    public RequestPage deleteSavedData() {
        try {
            Files.delete(Path.of(System.getProperty("user.dir") + "\\" + TEMP_FILE_PATH));
        } catch (IOException ignored) {
        }
        return this;
    }

    @SneakyThrows
    public RequestPage checkLastReqRate(double expectedRate) {
        List<String> lines = Files.readAllLines(Path.of(TEMP_FILE_PATH), Charset.forName("windows-1251"));
        String str = lines.get(lines.size() - 1);

        double actualRate = Double.parseDouble(str.split(",")[9]);
        Assertions.assertThat(actualRate)
                .as(String.format("Проверить, что ставка последней заявки равна %s", expectedRate))
                .isEqualTo(expectedRate);
        str = lines.get(lines.size() - 2);
        getRowByRequestNumber(str.split(",")[0]);
        return this;
    }

    @SneakyThrows
    public RequestPage getRowByRequestNumber(String requestNumber) {
        String row;
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(TEMP_FILE_PATH))) {
            int counter = 0;
            do {
                row = reader.readLine();
                if (row.contains(requestNumber)) {
                    break;
                }
                counter++;
            } while (row != null);
            currentTrades().mouseActions().mouseClick(BasePoint.CENTER_TOP, 0, 200);
            new Actions(DesktopConfiguration.driver).keyDown(CONTROL).keyDown(END).keyUp(END).keyUp(CONTROL).perform();
            for (int i = 0; i < counter; i++) {
                DesktopUtils.pressKey(Keys.UP);
            }
        }
        return this;
    }

    public void addRequestNumber() {
        WebElement el = findByAutomationId("144");
        String data = el.getText();
        Pattern pattern = Pattern.compile("\\w*\\d{4,}");
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            requestNumbers.add(matcher.group());
        }
    }

    public void addRequestNumber(String num) {
        requestNumbers.add(num);
    }

    @Step("Создать заявку на покупку в Quik")
    public void makeRequestError(String percent, String quantity, String errorMassage) {
        DesktopUtils.pressKey(Keys.F2);
        percentBox().click();
        new Actions(DesktopConfiguration.driver).sendKeys(percent).perform();
        quantity().click();
        new Actions(DesktopConfiguration.driver).sendKeys(quantity).perform();
        okButton().click();
        okButton().click();
        checkLastDialogueMessageContains(errorMassage, "Проверить, что появилось сообщение по ошибке " + errorMassage);
    }
}
