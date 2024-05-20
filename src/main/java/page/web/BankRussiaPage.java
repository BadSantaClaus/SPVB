package page.web;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.UIAssertionError;
import constants.*;
import elements.columns.BankRussiaProcessColumn;
import elements.columns.BankRussiaStepsColumn;
import elements.columns.IHasColumnDescriptor;
import elements.web.UiButton;
import elements.web.UiDropDownWithCheckBox;
import elements.web.UiTable;
import elements.web.UiTextBox;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.dom4j.Document;
import org.dom4j.Node;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.interactions.Actions;
import utils.SpvbUtils;
import utils.WaitingUtils;
import utils.XmlUtils;
import utils.ZipUtils;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static constants.Status.COMPLETE;
import static constants.Status.ERROR;
import static elements.Components.*;
import static elements.columns.BankRussiaProcessColumn.*;
import static elements.columns.BankRussiaStepsColumn.STEP_NAME;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

@Slf4j
@SuppressWarnings({"UnusedReturnValue", "unchecked", "FieldCanBeLocal"})
public class BankRussiaPage extends BasePage {

    public static BankRussiaPage instance;
    private final By tablesSelector = By.tagName("table");
    private final String processTableName = "Список процессов";
    private final String stepsTableName = "Шаги процесса";
    private final UiTable processTable = table($$(tablesSelector).get(0), processTableName);

    private UiTable stepsTable() {
        return table($$(tablesSelector).get(1), stepsTableName);
    }

    private final UiButton downloadAuctionCbr = button(stepsTable().getCell("Экспорт заявок БР в QUIK (аукцион)",
            STEP_NAME, BankRussiaStepsColumn.DOWNLOAD), "Скачать файл CBR.tri экспорта заявок БР в QUIK (аукцион)");

    private final UiButton downloadExtraPlacementCbr = button(stepsTable().getCell("Экспорт заявок БР в QUIK (доразмещение)",
            STEP_NAME, BankRussiaStepsColumn.DOWNLOAD), "Скачать файл CBR.tri экспорта заявок БР в QUIK (доразмещение)");

    private final UiButton downloadRepoCbr = button(stepsTable().getCell("Экспорт в QUIK встречных заявок РЕПО",
            STEP_NAME, BankRussiaStepsColumn.DOWNLOAD), "Скачать файл CBR.tri экспорта встречных заявок РЕПО");
    private final UiTextBox processFilter = textBox(processTable.getContainer()
                    .$x("./../../preceding-sibling::div//p[text()='Поиск: ']/following-sibling::div//input"),
            "Поиск по таблице " + processTable.getName());

    private final UiTextBox stepsFilter = textBox($x("//h4//following::input"),
            "Поиск по таблице " + processTable.getName());


    public static BankRussiaPage getInstance() {
        if (instance == null) {
            instance = new BankRussiaPage();
        }
        return instance;
    }

    public BankRussiaPage filterProcesses(String processName) {
        processFilter.getElement().shouldBe(visible);
        processFilter.setValue(processName);
        return this;
    }

    public BankRussiaPage openProcess(String processName) {
        openProcess(processName, PROCESS_NAME, processName);
        return this;
    }

    public BankRussiaPage openProcess(String processName, BankRussiaProcessColumn column, String columnVal) {
        filterProcesses(processName);
        WaitingUtils.sleep(1);
        processTable.getCell(columnVal, column).should(interactable, ofMinutes(3)).click();
        log.info("Раскрыть шаги процесса {}", processName);
        return this;
    }

    public BankRussiaPage filter(String name) {
        stepsFilter.clear();
        stepsFilter.setValue(name);
        log.info("Отфильтровать шаги, содержащие в названии {}", name);
        return this;
    }

    public BankRussiaPage waitAllStepsFinished(int timeOut) {
        waitAllStepsFinished(new ArrayList<>(), timeOut, 1, 1);
        return this;
    }

    public BankRussiaPage waitAllStepsFinished(int timeOut, Runnable runnable) {
        waitAllStepsFinished(new ArrayList<>(), timeOut, 1, 1, runnable);
        return this;
    }

    public BankRussiaPage waitAllStepsFinished(int timeOut, List<String> excludeSteps, Runnable runnable) {
        waitAllStepsFinished(excludeSteps, timeOut, 5, 1, runnable);
        return this;
    }


    public BankRussiaPage waitAllStepsFinished(List<String> excludeSteps, int timeOut, int pollInterval, int initDelay) {
        SpvbUtils.step("Дождаться завершения шагов по процессу");
        Callable<Boolean> action = () -> {
            try {
                ElementsCollection statuses = stepsTable().getColumn(BankRussiaProcessColumn.STATUS);
                if (statuses.isEmpty())
                    return false;
                ElementsCollection notFinishedSteps = statuses.filter(not(text(COMPLETE.getValue())));
                ElementsCollection finishedSteps = statuses.filter(text(COMPLETE.getValue()));
                Assertions.assertThat(notFinishedSteps.filter(text(Status.ERROR.getValue())))
                        .describedAs("Проверить, что шаги не содержат ошибок")
                        .isEmpty();
                if (excludeSteps.isEmpty()) {
                    return finishedSteps.texts().equals(statuses.texts());
                } else {
                    return notFinishedSteps.size() == excludeSteps.size() || finishedSteps.texts().equals(statuses.texts());
                }
            } catch (StaleElementReferenceException | IndexOutOfBoundsException ignored) {
            }
            return false;
        };
        WaitingUtils.waitUntil(timeOut, pollInterval, initDelay, "Ожидание завершения шагов", action);
        scrollStepsTableBottom();
        WaitingUtils.sleep(3);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Дождаться завершения '{countSteps}' шагов по процессу")
    public BankRussiaPage waitCountStepsFinished(String containsText, int countSteps, String status, Runnable runnable) {
        log.info("Дождаться завершения '{}' шагов по процессу", countSteps);
        runnable.run();
        WaitingUtils.waitUntil(5 * 60, 5, 1, "Ожидание завершения шагов", () -> {
            stepsTable().getCell(containsText, STEP_NAME).shouldBe(visible, Duration.ofSeconds(20));
            List<Map<IHasColumnDescriptor, SelenideElement>> actualList = stepsTable().getRowMapList(STEP_NAME,
                    BankRussiaStepsColumn.values(), containsText);
            List<String> actualStatuses = new ArrayList<>();
            actualList.stream()
                    .limit(4)
                    .forEach(m -> {
                                if (m.get(BankRussiaStepsColumn.STATUS).getText().equals(status)) {
                                    actualStatuses.add(m.get(BankRussiaStepsColumn.STATUS).getText());
                                }
                            }
                    );

            Assertions.assertThat(actualStatuses)
                    .describedAs("Проверить, что шаги не содержат ошибок")
                    .doesNotContain(ERROR.getValue());

            if (actualStatuses.size() == countSteps) {
                return true;
            } else {
                Selenide.refresh();
                runnable.run();
                return false;
            }
        });
        WaitingUtils.sleep(3);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Дождаться завершения шагов по процессу")
    public BankRussiaPage waitAllStepsFinished(List<String> excludeSteps, int timeOut, int pollInterval, int initDelay,
                                               Runnable runnable) {
        log.info("Дождаться завершения шагов по процессу");
        Callable<Boolean> action = () -> {
            try {
                ElementsCollection statuses = stepsTable().getColumn(BankRussiaProcessColumn.STATUS);
                if (statuses.isEmpty())
                    return false;
                ElementsCollection notFinishedSteps = statuses.filter(not(text(COMPLETE.getValue())));
                ElementsCollection finishedSteps = statuses.filter(text(COMPLETE.getValue()));
                Assertions.assertThat(notFinishedSteps.filter(text(Status.ERROR.getValue())))
                        .describedAs("Проверить, что шаги не содержат ошибок")
                        .isEmpty();
                if (excludeSteps.isEmpty()) {
                    if (finishedSteps.texts().equals(statuses.texts())) {
                        return true;
                    } else {
                        Selenide.refresh();
                        runnable.run();
                    }
                } else {
                    if (notFinishedSteps.size() == excludeSteps.size() || finishedSteps.texts().equals(statuses.texts())) {
                        return true;
                    } else {
                        Selenide.refresh();
                        runnable.run();
                    }
                }
            } catch (StaleElementReferenceException ignored) {
            }
            return false;
        };
        WaitingUtils.waitUntil(timeOut, pollInterval, initDelay, "Ожидание завершения шагов", action);

        scrollStepsTableBottom();
        WaitingUtils.sleep(3);
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BankRussiaPage checkPfx41Status(String fileName, Runnable runnable) {
        stepsFilter.setValue(fileName.substring(0, fileName.lastIndexOf('.')));
        waitAllStepsFinished(3 * 60, runnable);
        return this;
    }

    public BankRussiaPage expandProcessName(String processName) {
        WaitingUtils.waitUntil(3 * 60, 10, 1,
                "Ожидание появления элемента \"Расширить строку\"",
                () -> processTable.getCell(processName, PROCESS_NAME).$x(".//button").is(interactable));

        return expandProcess(processName);
    }

    public BankRussiaPage expandProcessName(String containsText, String waitingProcessName) {
        Allure.step("Ожидание появления элемента \"Расширить строку\"",
                () -> WaitingUtils.waitUntil(3 * 60, 10, 10,
                        "Ожидание появления элемента \"Расширить строку\"",
                        () -> {
                            processTable.getCell(containsText, PROCESS_NAME).$x(".//button").click();
                            if (processTable.getCell(containsText, PROCESS_NAME).$x(".//button").is(exist)) {
                                return true;
                            } else {
                                Selenide.refresh();
                                filterProcesses(waitingProcessName);
                                return false;
                            }
                        }));
        return expandProcess(containsText);
    }

    public BankRussiaPage expandProcess(String processName) {
        UiButton expand = button(processTable.getCell(processName, PROCESS_NAME).$x(".//button"),
                "Расширить строку");
        String expandParentClass = expand.getElement().$x("./..").getAttribute("class");
        if (Objects.requireNonNull(expandParentClass).matches(".*jss.*jss.*")) {
            expand.scrollIntoView();
            expand.click();
        }
        return this;
    }

    public BankRussiaPage checkFinishedStatus(String containsText, List<String> excludeSteps, Runnable runnable) {
        Allure.step(String.format("Ожидание появления элемента \"%s\"", containsText),
                () -> WaitingUtils.waitUntil(3 * 60, 10, 1,
                        String.format("Ожидание появления элемента \"%s\"", containsText),
                        () -> {
                            if (processTable.getCell(containsText, PROCESS_NAME).is(visible)) {
                                return true;
                            } else {
                                Selenide.refresh();
                                runnable.run();
                                return false;
                            }
                        }));

        processTable.getCell(containsText, PROCESS_NAME).click();
        scrollStepsTableBottom();

        waitAllStepsFinished(5 * 60, excludeSteps, () -> {
            runnable.run();
            processTable.getCell(containsText, PROCESS_NAME).click();
            scrollStepsTableBottom();
        });
        return this;
    }

    public BankRussiaPage checkAllStepsFinishedStatus(String containsText, List<String> excludeSteps, Runnable runnable) {
        checkFinishedStatus(containsText, excludeSteps, runnable);
        return this;
    }

    public BankRussiaPage checkAllStepsFinishedStatus(String containsText, Runnable runnable) {
        checkFinishedStatus(containsText, new ArrayList<>(), runnable);
        return this;
    }

    public BankRussiaPage checkPfx42Status(String processName, String pfx42Name) {
        openProcess(processName);
        scrollStepsTableBottom();
        SelenideElement startRow = stepsTable().getRowElements(String.format("PFX42: Получение документа из БР (%s)", pfx42Name), STEP_NAME).first();
        int startNumber = Integer.parseInt(startRow.$x(BankRussiaStepsColumn.NUMBER.getXPath()).getText());
        int endNumber = startNumber + 6;

        Allure.step("Ожидание завершения шагов",
                () -> WaitingUtils.waitUntil(5 * 60, 10, 10, "Ожидание завершения шагов",
                        () -> {
                            if (stepsTable().getContainer().$$x(".//tr").get(endNumber).$x(BankRussiaStepsColumn.STATUS.getXPath()).getText().equals(COMPLETE.getValue())) {
                                return true;
                            } else {
                                Selenide.refresh();
                                openProcess(processName);
                                scrollStepsTableBottom();
                                return false;
                            }
                        }));

        ElementsCollection allStepsRow = stepsTable().getContainer().$$x(".//tr");
        for (int i = startNumber; i <= endNumber; i++) {
            Assertions.assertThat(allStepsRow.get(i).$x(BankRussiaStepsColumn.STATUS.getXPath()).getText())
                    .describedAs("Проверить, что все шаги процесса \"PFX42. Получение из БР параметров и расписания дополнительного размещения успешно завершены\"")
                    .isEqualTo(COMPLETE.getValue());
        }
        scrollStepsTableBottom();
        WaitingUtils.sleep(3);
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BankRussiaPage checkProcessIsActive(FileName pfxPath, String containsText, String assertMessage, Runnable action) {
        Document pfx = XmlUtils.parseXml(pfxPath);
        WaitingUtils.waitUntil(60, 5, 5, assertMessage,
                () -> {
                    if ($x(String.format("//div[contains(text(), '%s')]", containsText)).isDisplayed()) {
                        List<String> actual = $$x(String.format("//div[contains(text(), '%s')]", containsText)).texts();
                        List<String> expected = new ArrayList<>();
                        List<Node> stocks = pfx.selectNodes("//AUCTION/@SECURITYID");
                        stocks.forEach(node -> expected.add(String.format(containsText + "%s)", node.getText())));
                        if (new HashSet<>(actual).containsAll(expected)) {
                            return true;
                        } else {
                            Selenide.refresh();
                            action.run();
                            return false;
                        }
                    } else {
                        Selenide.refresh();
                        action.run();
                        return false;
                    }
                });
        WaitingUtils.sleep(3);
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BankRussiaPage checkProcessStartTime(String containsText, String pfx42Name, String stockCode) {
        String actualTime = processTable.getCell(containsText, PROCESS_NAME, PLANNED_START_TIME).getText();
        LocalDateTime actual = LocalDateTime.parse(actualTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        LocalDateTime expected = XmlUtils.getTimeFromPfx(String.format(FilePath.TEMP_FILES_FORMAT.getValue(), pfx42Name), "START_TIME", stockCode).minusMinutes(1);
        Assertions.assertThat(actual)
                .describedAs("Проверить, что время начала процесса в файле соответствует планируемому времени старта на странице")
                .isEqualTo(expected);
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BankRussiaPage checkProcessStatus(String processName, String expectedStatus) {
        SelenideElement status = processTable.getCell(processName, PROCESS_NAME, BankRussiaProcessColumn.STATUS);
        Assertions.assertThat(status.getText())
                .isEqualTo(expectedStatus)
                .describedAs("Проверить статус процесса");
        return this;
    }

    public BankRussiaPage clearProcessFilter() {
        processFilter.clear();
        return this;
    }

    public BankRussiaPage openChildProcess(String parentName, String childName) {
        expandProcessName(parentName);
        processTable.getProcessChild(parentName, childName).should(enabled, ofSeconds(10)).click();
        SpvbUtils.step("Раскрыть вложенный процесс " + childName);
        return this;
    }

    public BankRussiaPage waitProcessWithStepFinished(String processName, String step, int timeOut) {
        //может быть несколько процессов с одинаковым названием
        WaitingUtils.waitUntil(timeOut, 1, 1,
                String.format("Ожидание процесса %s, содержащего в шагах %s", processName, step),
                () -> {
                    ElementsCollection rows = processTable.getRowElements(processName, PROCESS_NAME);

                    for (SelenideElement el : rows) {
                        try {
                            processTable.getCell(el, PROCESS_NAME)
                                    .scrollIntoView(false).click();
                            filter(step);
                            stepsTable().getRowElements(step, STEP_NAME).should(sizeGreaterThan(0), ofSeconds(3));
                            return true;
                        } catch (UIAssertionError | Exception ignored) {
                        }

                    }
                    return false;
                });
        waitAllStepsFinished(5);
        return this;
    }

    public BankRussiaPage checkListStepsOfProcess(List<String> expSteps) {
        if (expSteps.isEmpty())
            return this;

        List<String> actSteps = stepsTable().getColumn(STEP_NAME).shouldBe(size(expSteps.size()), ofMinutes(10)).texts();
        Assertions.assertThat(actSteps)
                .isEqualTo(expSteps);
        return this;
    }

    @SneakyThrows
    public File downloadAuctionCbrFile() {
        return ZipUtils.unzipFileWithInternalZip(downloadAuctionCbr.download());
    }

    public File downloadRepoCbrFile() {
        return ZipUtils.unzipFileWithInternalZip(downloadRepoCbr.download());
    }

    @SneakyThrows
    public File downloadExtraPlacementCbr() {
        return ZipUtils.unzipFileWithInternalZip(downloadExtraPlacementCbr.download());
    }

    public BankRussiaPage waitProcessComplete(String processName) {
        Allure.step("Ожидание завершения процесса " + processName,
                () -> WaitingUtils.waitUntil(5 * 60, 20, 20, "Ожидание завершения процесса " + processName, () -> {
                    String actual = processTable.getCell(processName, PROCESS_NAME,
                            BankRussiaProcessColumn.STATUS).getText();
                    Assertions.assertThat(actual)
                            .describedAs(String.format("Проверить, что процесс \"%s\" прошел без ошибок", processName))
                            .isNotEqualTo(Status.ERROR.getValue());
                    if (actual.equals(COMPLETE.getValue())) {
                        return true;
                    } else {
                        Selenide.refresh();
                        processTable.getContainer().$x("./../../preceding-sibling::div//p[text()='Поиск: ']/following-sibling::div//input").setValue(processName);
                        return false;
                    }
                }));
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BankRussiaPage waitProcessFail(String processName) {
        Allure.step("Ожидание завершения процесса " + processName,
                () -> WaitingUtils.waitUntil(5 * 60, 20, 20, "Ожидание завершения процесса " + processName, () -> {
                    String actual = processTable.getCell(processName, PROCESS_NAME,
                            BankRussiaProcessColumn.STATUS).getText();
                    Assertions.assertThat(actual)
                            .describedAs(String.format("Проверить, что процесс \"%s\" завершился ошибкой", processName))
                            .isNotEqualTo(COMPLETE.getValue());
                    if (actual.equals(ERROR.getValue())) {
                        return true;
                    } else {
                        Selenide.refresh();
                        filterProcesses(processName);
                        return false;
                    }
                }));
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BankRussiaPage scrollProcessTableBottom() {
        processTable.getContainer().$$x(".//tr").last().scrollTo();
        new Actions(WebDriverRunner.getWebDriver()).keyDown(Keys.END).keyUp(Keys.END).perform();
        return this;
    }

    public BankRussiaPage scrollStepsTableBottom() {
        stepsTable().getContainer().$$x(".//tr").first().click();
        new Actions(WebDriverRunner.getWebDriver()).keyDown(Keys.END).keyUp(Keys.END).perform();
        new Actions(WebDriverRunner.getWebDriver()).keyDown(Keys.END).keyUp(Keys.END).perform();
        return this;
    }


    public BankRussiaPage waitStepWithColumn(String stepName, BankRussiaStepsColumn column, String val) {
        log.info("Ожидание шага процесса со статусом - {}", stepName);
        WaitingUtils.waitUntil(5 * 60, 20, 1, "Ожидание завершения процесса " + stepName, () -> {
            stepsFilter.setValue(stepName);
            SelenideElement actual = stepsTable().getCell(stepName, STEP_NAME,
                    column);
            if (column.equals(BankRussiaStepsColumn.OPERATING_MODE)) {
                return actual.isSelected() == Boolean.parseBoolean(val);
            }
            return actual.getText().equals(val);
        });
        return this;
    }

    public BankRussiaPage setAppGetTime(Map<String, Boolean> times) {
        UiDropDownWithCheckBox timeGetApps = new UiDropDownWithCheckBox(
                $x("//p[text()='Время сбора заявок']/following-sibling::div//input"),
                "Поиск времени сбора заявок"
        );

        timeGetApps.selectOptions(times);

        return this;
    }

    public LocalDateTime getTimeFromUi(String containsText, BankRussiaProcessColumn column) {
        String attr = processTable.getCell(containsText, PROCESS_NAME, column).getText();
        attr = attr.substring(attr.lastIndexOf(" ")).trim();
        LocalTime result = LocalTime.parse(attr, DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue()));
        return result.atDate(LocalDate.now());
    }

    public BankRussiaPage waitProcessStatus(String processName, String alias, Status status, LocalDateTime plannedStartTime) {
        Allure.step(alias,
                () -> WaitingUtils.waitUntil(5 * 60, 10, 0, alias, () -> {
                    Selenide.refresh();
                    filterProcesses(processName);
                    processTable.getCell(processName, PROCESS_NAME).shouldBe(visible, Duration.ofSeconds(20));
                    List<Map<IHasColumnDescriptor, SelenideElement>> actualList = processTable.getRowMapList(PROCESS_NAME, BankRussiaProcessColumn.values(), processName);

                    actualList = actualList.stream()
                            .filter(m -> m.get(PLANNED_START_TIME).getText().equals(plannedStartTime.format(DateTimeFormatter.ofPattern(DateFormat.WEB_DATE_TIME_FORMAT.getValue()))))
                            .toList();

                    actualList.forEach(m -> log.info(m.get(PLANNED_START_TIME).getText()));

                    Assertions.assertThat(actualList.size())
                            .describedAs(String.format("Проверить, что существует только 1 процесс '%s' запускаемый во время '%s'", processName, plannedStartTime))
                            .isEqualTo(1);
                    String actual = actualList.get(0).get(STATUS).getText();
                    return actual.equals(status.getValue());
                }));
        WaitingUtils.sleep(3);
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BankRussiaPage checkOperatingMode(String processName, OperatingMode operatingMode, LocalDateTime startTime) {
        List<Map<IHasColumnDescriptor, SelenideElement>> actualList = processTable.getRowMapList(PROCESS_NAME, BankRussiaProcessColumn.values(), processName);
        actualList = actualList.stream()
                .filter(m -> m.get(PLANNED_START_TIME).getText().equals(startTime.format(DateTimeFormatter.ofPattern(DateFormat.WEB_DATE_TIME_FORMAT.getValue()))))
                .toList();
        Assertions.assertThat(actualList.size())
                .describedAs(String.format("Проверить, что существует только 1 процесс '%s' запускаемый во время '%s'", processName, startTime))
                .isEqualTo(1);
        String actual = actualList.get(0).get(OPERATING_MODE).getAttribute("class");
        switch (operatingMode) {
            case AUTO -> Assertions.assertThat(actual)
                    .describedAs("Проверить, что установлен режим работы Авто")
                    .contains("checked");
            case MANUAL -> Assertions.assertThat(actual)
                    .describedAs("Проверить, что установлен режим работы Ручной")
                    .doesNotContain("checked");
        }
        WaitingUtils.sleep(3);
        SpvbUtils.takeScreenshot();
        return this;
    }
}
