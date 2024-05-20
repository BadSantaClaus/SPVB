package page.web;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import constants.DateFormat;
import elements.columns.AuditColumn;
import elements.web.UiButton;
import elements.web.UiCheckbox;
import elements.web.UiTable;
import elements.web.UiTextBox;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.interactions.Actions;
import utils.SpvbUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.codeborne.selenide.Selenide.$x;

@Getter
@SuppressWarnings({"UnusedReturnValue", "deprecation"})
public class AuditPage extends BasePage {

    private final UiTextBox dateTime = new UiTextBox($x("//input[@name = 'from_date']"), "Дата и время");

    private final UiButton cleanDate = new UiButton($x("//input[@name = 'from_date']/following-sibling::button"), "Очистить дату");

    private final UiButton filter = new UiButton($x("//span[text() = 'Настройка фильтров']"), "Настройка фильтров");

    private final UiCheckbox operationName = new UiCheckbox(
            $x("//span[text() = 'Наименование операции']//ancestor::label//input"), "Наименование операции");

    private final UiTextBox operationNameField = new UiTextBox(
            $x("//label[text() = 'Наименование операции']//following::input"), "Наименование операции");

    private final UiButton confirm = new UiButton($x("//span[text() = 'Подтвердить']"), "Подтвердить");

    private final UiButton find = new UiButton($x("//span[text() = 'Найти']"), "Найти");

    private final UiTable table = new UiTable($x("//table"), "Таблица аудита");

    private final SelenideElement forward = $x("//div[@title = 'Вперед']//button");

    public static AuditPage instance;

    public static AuditPage getInstance() {
        if (instance == null) {
            instance = new AuditPage();
        }
        return instance;
    }

    public AuditPage addOperationNameFilter() {
        filter.click();
        operationName.setValue(true);
        confirm.click();
        return this;
    }

    public AuditPage setCurrentDate(LocalDateTime startTime) {
        dateTime.getElement().shouldBe(Condition.clickable);
        dateTime.sendKeys(startTime.format(DateTimeFormatter.ofPattern(DateFormat.AUDIT_DATE_TIME_FORMAT.getValue())));
        return this;
    }

    public AuditPage setOperationName(String operationName) {
        operationNameField.setValue(operationName);
        return this;
    }

    public AuditPage find() {
        find.click();
        return this;
    }

    public AuditPage checkStatuses(List<String> allowedStatuses, SoftAssertions assertions, int page) {
        List<String> actual = table.getContainer().$$x(".//td[position() = 5]").texts();
        assertions.assertThat(actual)
                .describedAs(String.format("Проверить, что страница '%d' содержит только статусы %s", page, allowedStatuses.toString()))
                .containsOnlyElementsOf(allowedStatuses);
        SpvbUtils.takeNameScreenshot(String.format("Страница - %d", page));
        return this;
    }

    public AuditPage checkAllPagesByStatus(List<String> allowedStatuses) {
        int page = 1;
        SoftAssertions assertions = new SoftAssertions();
        checkStatuses(allowedStatuses, assertions, page);
        Actions actions = new Actions(WebDriverRunner.getWebDriver());
        while (forward.is(Condition.visible, Duration.ofSeconds(20))) {
            forward.click();
            page++;
            checkStatuses(allowedStatuses, assertions, page);
            actions.moveToElement(table.getContainer()).perform();
        }
        assertions.assertAll();
        return this;
    }

    public AuditPage checkLastEventDescription(String message, String operationName) {
        SelenideElement eventDescription = table.getCell(operationName, AuditColumn.OPERATION_NAME, AuditColumn.EVENT_DESCRIPTION);
        eventDescription.click();
        String actual = table.getContainer().$x(".//p[text() = 'Описание события']//following-sibling::p").getText();
        Assertions.assertThat(actual)
                .describedAs(String.format("Проверить, что Описание события содержит сообщение '%s'", message))
                .isEqualTo(message);
        SpvbUtils.takeScreenshot();
        return this;
    }

}
