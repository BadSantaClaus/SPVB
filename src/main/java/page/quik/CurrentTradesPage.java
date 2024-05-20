package page.quik;

import FlaNium.WinAPI.elements.Button;
import FlaNium.WinAPI.elements.ListBoxItem;
import FlaNium.WinAPI.elements.TextBox;
import FlaNium.WinAPI.enums.BasePoint;
import config.DesktopConfiguration;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import utils.DesktopUtils;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static elements.Components.*;
import static org.openqa.selenium.Keys.*;
import static utils.DesktopUtils.*;
import static utils.WaitingUtils.sleep;

@SuppressWarnings("UnusedReturnValue")
public class CurrentTradesPage extends BaseQuikPage {

    public TextBox availableStocks() {
        return desktopTextBox(findByControlType("Pane"), "//*[(@ClassName = 'Edit')]", "Доступные инструменты");
    }

    public Button addAllParameters() {
        return desktopButton(findByAutomationId("10421"), "Добавить все доступные параметры");
    }

    public Button addStocks() {
        return desktopButton(findByAutomationId("10404"), "Добавить выбранные доступные инструменты");
    }

    public ListBoxItem auctionListItem(String stockName) {
        return listBoxItem(findByAutomationId("10402"), "//*[contains(@Name, 'Аукцион')]", stockName);
    }

    public ListBoxItem stocksListItem(String stockName) {
        return listBoxItem(findByAutomationId("10402"), String.format("//*[(@Name = '%s')]", stockName), stockName);
    }

    public ListBoxItem auctionListItemContains(String stockName) {
        return listBoxItem(findByAutomationId("10402"), String.format("//*[contains(@Name, '%s')]", stockName), stockName);
    }

    private final String TEMP_FILE_PATH = "target\\temp\\table.txt";

    @Step("Открыть таблицу текущих торогов")
    public CurrentTradesPage openCurrentTrades(String stockName) {
        int tableWaiting = 1;
        currentTrades().click();
        availableStocks().setText(stockName);
        stocksListItem(stockName).click();
        addStocks().click();
        addAllParameters().click();
        okButton().click();
        sleep(tableWaiting);
        return this;
    }

    @Step("Открыть таблицу текущих торогов")
    public CurrentTradesPage openAuctionCurrentTrades(String stockName) {
        int tableWaiting = 1;
        currentTrades().click();
        availableStocks().setText(stockName);
        auctionListItem(stockName).click();
        pressKey(DOWN);
        addStocks().click();
        addAllParameters().click();
        okButton().click();
        sleep(tableWaiting);
        return this;
    }

    public CurrentTradesPage openCreateTableWindow() {
        currentTrades().click();
        return this;
    }

    public CurrentTradesPage checkTradesContainsExist(String stockName, boolean isExist) {
        availableStocks().setText(stockName);
        if (isExist)
            Assertions.assertThat(auctionListItemContains(stockName))
                    .isNotNull()
                    .describedAs("Проверить, что инструмент " + stockName + "есть в списке доступных");
        else
            Assertions.assertThatThrownBy(() -> auctionListItemContains(stockName))
                    .isInstanceOf(org.openqa.selenium.NoSuchElementException.class)
                    .describedAs("Проверить, что инструмента " + stockName + "нет в списке доступных");
        return this;
    }

    @Step("Проверить, что ценная бумага \"{stockName}\" находится в статусе \"торгуется\"")
    public CurrentTradesPage checkTradingStatus(String stockName) {
        WaitingUtils.sleep(1);
        DesktopUtils.takeScreenshot();
        saveToFile();
        sleep(1);
        Assertions.assertThat(SpvbUtils.readFromFile(TEMP_FILE_PATH))
                .describedAs(String.format("Проверить, что ценная бумага \"%s\" находится в статусе \"торгуется\"", stockName))
                .containsIgnoringCase("торгуется");
        currentTrades().mouseActions().mouseClick(BasePoint.CENTER_TOP, 0, 200);
        return this;
    }

    public void saveToFile() {
        try {
            Files.delete(Path.of(System.getProperty("user.dir") + "\\" + TEMP_FILE_PATH));
        } catch (IOException ignored) {
        }
        currentTrades().mouseActions().mouseRightClick(BasePoint.CENTER_TOP, 0, 200);
        WaitingUtils.sleep(1);
        DesktopUtils.pressKey(Keys.UP);
        DesktopUtils.pressKey(Keys.UP);
        DesktopUtils.pressKey(Keys.UP);
        DesktopUtils.pressKey(Keys.ENTER);
        new TextBox(findByAutomationId("1001", "(@ControlType = 'Edit')")).setText(System.getProperty("user.dir") + "\\" + TEMP_FILE_PATH);
        okButton().click();
        WaitingUtils.sleep(1);
    }


    @SneakyThrows
    public void getRowByStock(String stockName) {
        try (FileInputStream f = new FileInputStream(TEMP_FILE_PATH)) {
            String file = IOUtils.toString(f, "Windows-1251");
            try (Scanner scanner = new Scanner(file)) {
                String row;
                int counter = 0;
                do {
                    row = scanner.nextLine();
                    if (row.contains(stockName)) {
                        Assertions.assertThat(row.contains("торгуется"))
                                .as("Проверить, что ценная бумага " + stockName + " находится в статусе \"торгуется\"")
                                .isTrue();
                        break;
                    }
                    counter++;
                } while (row != null);
                currentTrades().mouseActions().mouseClick(BasePoint.CENTER_TOP, 0, 200);
                new Actions(DesktopConfiguration.driver).keyDown(CONTROL).keyDown(HOME).keyUp(HOME).keyUp(CONTROL).perform();
                for (int i = 1; i < counter; i++) {
                    DesktopUtils.pressKey(DOWN);
                }
            }
        }
    }
}
