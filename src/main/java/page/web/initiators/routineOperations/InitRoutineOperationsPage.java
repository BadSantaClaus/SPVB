package page.web.initiators.routineOperations;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.WebDriverRunner;
import constants.Status;
import elements.columns.InitRoutineOperationsColumn;
import elements.web.UiTable;
import elements.web.UiTextBox;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import page.web.initiators.InitiatorsPage;
import utils.WaitingUtils;

import static com.codeborne.selenide.Selenide.$x;

@SuppressWarnings("UnusedReturnValue")
public class InitRoutineOperationsPage extends InitiatorsPage {

    private static InitRoutineOperationsPage instance;

    public String searchSelector = "//*[text()='Поиск: ']/following-sibling::*//input";
    public UiTextBox searchProcess = new UiTextBox($x(searchSelector), "Поиск процесса");

    public UiTable processTable = new UiTable($x("(//table)[1]"), "Список процессов");

    public static InitRoutineOperationsPage getInstance() {
        if (instance == null)
            instance = new InitRoutineOperationsPage();
        return instance;
    }

    public InitRoutineOperationsPage filterProcess(String processName) {
        searchProcess.setValue(processName);
        return this;
    }

    public InitRoutineOperationsPage checkLastProcessesFinished() {
        ElementsCollection statuses = processTable.getColumn(InitRoutineOperationsColumn.STATUS);
        WaitingUtils.waitUntil(
                3 * 60, 1, 1,
                "Дождаться, что все процессы будут в статусе 'Завершен'",
                () -> statuses.get(statuses.size()-1).getText().contains(Status.COMPLETE.getValue())
        );
        return this;
    }

    public int countFinishedProcess(String stockName){
        processTable.getColumn(InitRoutineOperationsColumn.STATUS);
        WaitingUtils.waitUntil(
                60, 1, 1,
                "Дождаться, применения фильтра",
                () -> processTable.getColumn(InitRoutineOperationsColumn.PROCESS_NAME).filter(Condition.text("" +
                        "Проведение депозитного аукциона (Код БИ: " + stockName + ")")).size() +
                        processTable.getColumn(InitRoutineOperationsColumn.PROCESS_NAME).filter(Condition.text("" +
                                "Проведение депозитных торгов (Код БИ: " + stockName + ")")).size() ==
                        processTable.getColumn(InitRoutineOperationsColumn.PROCESS_NAME).size() ||
                        processTable.getColumn(InitRoutineOperationsColumn.PROCESS_NAME).get(0).getText().contains("Нет данных")
        );
        return processTable.getColumn(InitRoutineOperationsColumn.STATUS).filter(Condition.text("Завершен")).size();
    }

    public InitRoutineOperationsPage checkLastProcessFinished() {
        $x(".//tr[1]").click();
        new Actions(WebDriverRunner.getWebDriver()).keyDown(Keys.END).keyUp(Keys.END).perform();
        new Actions(WebDriverRunner.getWebDriver()).keyDown(Keys.END).keyUp(Keys.END).perform();
        WaitingUtils.waitUntil(
                3 * 60, 1, 1, "Ожидание, что процесс находится в статусе 'Завершен'", () -> {
                    ElementsCollection statuses = processTable.getColumn(InitRoutineOperationsColumn.STATUS);
                    String actual = statuses.last().getText();
                    return actual.equals(Status.COMPLETE.getValue());
                }
        );
        return this;
    }


    public InitRoutineOperationsPage checkAllProcessesNotExist() {
        WaitingUtils.waitUntil(
                3 * 60, 1, 1,
                "Дождаться, что в таблице нет данных",
                () -> processTable.isTableEmpty()
        );
        return this;
    }
}
