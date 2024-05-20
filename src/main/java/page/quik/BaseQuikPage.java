package page.quik;

import FlaNium.WinAPI.elements.Button;
import FlaNium.WinAPI.elements.ListBoxItem;
import FlaNium.WinAPI.elements.TextBox;
import config.DesktopConfiguration;
import constants.QuikMainMenu;
import constants.QuikMainMenuItems;
import elements.desktop.DesktopButton;
import lombok.Getter;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebElement;
import utils.DesktopUtils;
import utils.SpvbUtils;
import utils.WaitingUtils;

import static elements.Components.*;
import static org.openqa.selenium.Keys.*;
import static utils.DesktopUtils.*;

@Getter
public class BaseQuikPage {

    public Button okButton() {
        return desktopButton(findByAutomationId("1", "(@ControlType = 'Button')"), "Подтвердить");
    }

    public Button cancelButton() {
        return desktopButton(findByAutomationId("2", "(@ControlType = 'Button')"), "Отменить");
    }

    public Button currentTrades() {
        return desktopButton(findByName("Создать новую таблицу текущих торгов"), "Создать новую таблицу текущих торгов");
    }

    public ListBoxItem outStockExchange() {
        return listBoxItem(findByAutomationId("33383"), "//*[(@Name = 'Внебиржевые заявки')]", "Внебиржевые заявки");
    }

    public Button newWindow() {
        return desktopButton(findByName("Создать новое окно"), "Создать новое окно");
    }

    public TextBox pathData() {
        DesktopConfiguration.driver.setDesktopAsRootElement();
        return desktopTextBox(findByAutomationId("1148", "(@ControlType = 'Edit')"), "Путь к файлу");
    }

    public Button searchInstrumentsButton() {
        return new DesktopButton(findByNameContains("Найти все инструменты, в названии или коде которых встречается указанная комбинация символов."), "Поиск инструментов");
    }

    @SneakyThrows
    public void openImportTransactionFromFile() {
        WaitingUtils.sleep(1);
        pressKey(ALT);
        pressKey(LEFT);
        pressKey(LEFT);
        pressKey(DOWN);
        pressKey(DOWN);
        pressKey(DOWN);
        pressKey(RIGHT);
        pressKey(UP);
        pressKey(UP);
        pressKey(ENTER);
    }

    public void openMainMenuTab(QuikMainMenu mainMenu) {
        SpvbUtils.step(String.format("Раскрыть меню %s", mainMenu.getValue()));
        DesktopUtils.pressKey(ALT);
        for (int i = 0; i < mainMenu.getNumFromLeft(); i++) {
            DesktopUtils.pressKey(LEFT);
        }
    }

    public void chooseMainMenuTabItem(QuikMainMenuItems item) {
        SpvbUtils.step(String.format("Выбрать пункт %s", item.getValue()));
        for (int i = 0; i < item.getNumFromUp(); i++) {
            DesktopUtils.pressKey(DOWN);
        }
        DesktopUtils.pressKey(ENTER);
    }

    public void checkLastDialogueMessageContains(String expected, String assertMessage) {
        DesktopUtils.takeScreenshot();
        WebElement el = findByAutomationId("144");
        WaitingUtils.waitUntil(10, 1, 1, assertMessage + " текущее сообщение: " + el.getText(),
                () -> el.getText().contains(expected));
    }

    public void checkLastDialogueMessageNotContains(String expected, String assertMessage) {
        DesktopUtils.takeScreenshot();
        WebElement el = findByAutomationId("144");
        Assertions.assertThat(el.getText())
                .describedAs(assertMessage)
                .doesNotContainIgnoringCase(expected);
    }

}
