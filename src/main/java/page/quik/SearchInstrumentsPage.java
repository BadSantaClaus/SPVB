package page.quik;

import FlaNium.WinAPI.DesktopElement;
import FlaNium.WinAPI.elements.Button;
import FlaNium.WinAPI.elements.TextBox;
import FlaNium.WinAPI.elements.Window;
import FlaNium.WinAPI.enums.BasePoint;
import config.DesktopConfiguration;
import lombok.SneakyThrows;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import utils.DesktopUtils;
import utils.WaitingUtils;

import static elements.Components.desktopButton;
import static elements.Components.desktopTextBox;
import static utils.DesktopUtils.findByAutomationId;
import static utils.DesktopUtils.findByName;

public class SearchInstrumentsPage extends BaseQuikPage {

    public TextBox searchTextBox() {
        return desktopTextBox(findByAutomationId("5556"), "Поиск инструмента");
    }

    public Button createTableQuotes() {
        String name = "Создать таблицу котировок для выбранного инструмента";
        return desktopButton(findByName(name), name);
    }

    @SneakyThrows
    public SearchInstrumentsPage openSearchInstrument() {
        for (int i = 0; i < 5; i++) {
            try {
                DesktopUtils.openSearchInstrument();
                WaitingUtils.waitUntil(5, 1, 1, "Ожидание появления поиска инструмента",
                        () -> searchTextBox().isEnabled());
                break;
            } catch (ConditionTimeoutException | AssertionError e) {
                if (i > 3)
                    throw e;
            }
        }
        return this;
    }

    @SneakyThrows
    public SearchInstrumentsPage searchInstrument(String instrument) {
        searchTextBox().setText(instrument);
        DesktopUtils.pressKey(Keys.ENTER);
        return this;
    }

    @SneakyThrows
    public SearchInstrumentsPage openInstrumentQuotes() {
        DesktopUtils.takeScreenshot();
        DesktopElement tablePane = new DesktopElement(findByAutomationId("125"));
        tablePane.mouseActions().mouseClick(BasePoint.CENTER, 0, 0);
        new Actions(DesktopConfiguration.driver)
                .keyDown(Keys.CONTROL)
                .keyDown(Keys.HOME)
                .keyUp(Keys.CONTROL)
                .keyUp(Keys.HOME).perform();
        createTableQuotes().click();
        return this;
    }

    public CreateAppPage openCreateAppPage() {
        new Window(findByAutomationId("50001").findElement(By.xpath("//Pane"))).mouseActions().mouseDoubleClick(BasePoint.CENTER, 0, 0);
        return new CreateAppPage();
    }
}
