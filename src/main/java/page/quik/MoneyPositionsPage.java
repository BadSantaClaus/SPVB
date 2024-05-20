package page.quik;

import FlaNium.WinAPI.DesktopElement;
import FlaNium.WinAPI.enums.BasePoint;
import config.DesktopConfiguration;
import elements.desktop.DesktopButton;
import elements.desktop.DesktopTextBox;
import org.awaitility.core.ConditionTimeoutException;

import java.io.File;

import static constants.FilePath.TEMP_FILES;
import static elements.Components.desktopButton;
import static elements.Components.desktopTextBox;
import static utils.DesktopUtils.findByAutomationId;
import static utils.DesktopUtils.findByName;

@SuppressWarnings("UnusedReturnValue")
public class MoneyPositionsPage extends BaseQuikPage {

    DesktopElement table = new DesktopElement(findByAutomationId("125"));

    DesktopTextBox pathToSave() {
        return desktopTextBox(findByAutomationId("30832"), "Путь для сохранения таблицы");
    }

    DesktopButton saveButton() {
        return desktopButton(findByAutomationId("30837"), "Кнопка Сохранить");
    }

    DesktopButton exitButton() {
        return desktopButton(okButton(), "Кнопка Выход");
    }

    public MoneyPositionsPage saveTableData(String fileToSave) {
        for (int i = 0; i < 5; i++) {
            try {
                table.mouseActions().mouseRightClick(BasePoint.CENTER, 0, 0);
                DesktopConfiguration.driver.setDesktopAsRootElement();
                DesktopButton menuItem = new DesktopButton(findByName("Сохранить в файл позиции из таблицы"), "Сохранить в файл позиции из таблицы");
                menuItem.click();
                break;
            } catch (ConditionTimeoutException | AssertionError e) {
                if (i > 3)
                    throw e;
            }
        }
        try {
            cancelButton().click();
        } catch (ConditionTimeoutException | AssertionError ignore) {
        }
        File file = new File(TEMP_FILES.getValue() + fileToSave);
        pathToSave().setText(file.getAbsolutePath());
        saveButton().click();
        exitButton().click();
        return this;
    }

}
