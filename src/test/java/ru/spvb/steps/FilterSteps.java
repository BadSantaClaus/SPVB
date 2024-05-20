package ru.spvb.steps;

import com.codeborne.selenide.Selenide;
import constants.DateFormat;
import elements.columns.IHasColumnDescriptor;
import io.qameta.allure.Step;
import org.assertj.core.api.SoftAssertions;
import page.web.initiators.InitiatorsPage;
import page.web.initiators.auction.FilterInitiators;
import utils.AllureEdit;
import utils.SpvbUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.$x;

@SuppressWarnings("UnusedReturnValue")
public class FilterSteps {

    @Step("Выставить фильтр")
    public FilterSteps setFilter(InitiatorsPage.FilterColumns column, List<String> val) {
        FilterInitiators.getInstance().setFilter(column, val);
        AllureEdit.setParams(Map.of("filterColumn", column.getName()));
        return this;
    }

    @Step("Обновить страницу")
    public FilterSteps refreshPage() {
        Selenide.refresh();
        return this;
    }

    @Step("Проверить, что строки в столбце содержат значения")
    public FilterSteps checkColumnHasSameStrings(IHasColumnDescriptor column, List<String> expected, SoftAssertions softly) {
        AllureEdit.removeParamByName("softly");
        FilterInitiators.getInstance().checkColumnHasSameStrings(column, expected, softly);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Проверить, что строки на текущей странице содержат значения")
    public FilterSteps checkColumnHasSameStringsOnPage(IHasColumnDescriptor column, List<String> expected, SoftAssertions softly) {
        AllureEdit.removeParamByName("softly");
        FilterInitiators.getInstance().checkColumnHasSameStringsOnPage(column, expected, softly);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Проверить, что все строки в столбце содержат значение {expected}")
    public FilterSteps checkFilteredColumn(IHasColumnDescriptor column, String expected, SoftAssertions softly) {
        AllureEdit.setParams(Map.of(
                "column", column.getName(),
                "expected", expected
        ));
        FilterInitiators.getInstance().checkColumnHasString(column, expected, softly);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Проверить, что даты столбца в промежутке")
    public FilterSteps checkFilteredDatesBetween(IHasColumnDescriptor column,
                                                 LocalDateTime from, LocalDateTime to,
                                                 DateFormat format, SoftAssertions softly) {
        AllureEdit.setParams(Map.of(
                "column", column.getName(),
                "from", from.format(DateTimeFormatter.ofPattern(DateFormat.WEB_DATE_TIME_FORMAT.getValue())),
                "to", to.format(DateTimeFormatter.ofPattern(DateFormat.WEB_DATE_TIME_FORMAT.getValue()))
        ));
        FilterInitiators.getInstance().checkColumnDatesBetween(column, from, to, format, softly);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Проверить, что все строки подходят под множественный фильтр")
    public FilterSteps checkFilteredColumnContains(IHasColumnDescriptor column, List<String> expected, SoftAssertions softly) {
        FilterInitiators.getInstance().checkColumnContains(column, expected, softly);
        return this;
    }

    @Step("Проверить, что по фильтрам нет данных")
    public FilterSteps checkNoData(SoftAssertions softly) {
        softly.assertThat($x("//*[text()='Нет данных']").exists())
                .isTrue();
        return this;
    }
}
