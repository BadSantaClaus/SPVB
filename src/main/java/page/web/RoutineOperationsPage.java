package page.web;

import com.codeborne.selenide.SelenideElement;
import elements.web.UiButton;
import elements.web.UiTable;
import elements.web.UiTextBox;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import utils.WaitingUtils;

import static com.codeborne.selenide.Condition.interactable;
import static com.codeborne.selenide.Selenide.$$;
import static elements.Components.*;
import static elements.columns.RoutineOperationsProcessColumns.PROCESS_NAME;
import static elements.columns.RoutineOperationsProcessColumns.STATUS;

public class RoutineOperationsPage extends BasePage {

    public static RoutineOperationsPage instance;
    private final By tablesSelector = By.tagName("table");
    private final String processTableName = "Список процессов";
    private final String stepsTableName = "Шаги процесса";
    private final UiTable processTable = table($$(tablesSelector).get(0), processTableName);

    private UiTable stepsTable() {
        return table($$(tablesSelector).get(1), stepsTableName);
    }

    private final UiTextBox processFilter = textBox(processTable.getContainer()
                    .$x("./../../preceding-sibling::div//p[text()='Поиск: ']/following-sibling::div//input"),
            "Поиск по таблице " + processTable.getName());

    public static RoutineOperationsPage getInstance() {
        if (instance == null) {
            instance = new RoutineOperationsPage();
        }
        return instance;
    }

    public RoutineOperationsPage filterProcesses(String processName) {
        processFilter.setValue(processName);
        return this;
    }

    public RoutineOperationsPage expandProcessName(String processName) {
        WaitingUtils.waitUntil(5 * 60, 10, 1,
                "Ожидание появления элемента \"Расиширить строку\"",
                () -> button(processTable.getCell(processName, PROCESS_NAME)
                        .$x(".//button"), "Расширить строку").getElement().is(interactable));
        UiButton expand = button(processTable.getCell(processName, PROCESS_NAME).$x(".//button"),
                "Расширить строку");
        expand.click();
        return this;
    }

    public RoutineOperationsPage checkProcessStatus(String processName, String expectedStatus) {
        SelenideElement statusCell = processTable.getCell(processName, PROCESS_NAME, STATUS);
        Assertions.assertThat(statusCell.getText())
                .isEqualTo(expectedStatus)
                .describedAs("Проверить, что статус процесса равен " + processTable);
        return this;
    }
}
