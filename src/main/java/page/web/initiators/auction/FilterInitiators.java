package page.web.initiators.auction;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import constants.DateFormat;
import elements.columns.IHasColumnDescriptor;
import elements.web.*;
import io.qameta.allure.Step;
import org.assertj.core.api.SoftAssertions;
import page.web.initiators.InitiatorsPage;
import utils.AllureEdit;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.codeborne.selenide.Condition.interactable;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

@SuppressWarnings({"UnusedReturnValue"})
public class FilterInitiators extends InitiatorsPage{

    public static FilterInitiators instance;

    public static FilterInitiators getInstance(){
        if(instance==null)
            return new FilterInitiators();
        return instance;
    }

    UiButton search = new UiButton($x("//span[text()='Найти']/ancestor::button"), "Найти");
    UiButton nextPage = new UiButton($x("//div[@title='Вперед']//button"), "Следующая страница");

    private final String filterTemplate = "//label[text()='%s']/../following-sibling::div//input";

    public FilterInitiators setFilter(InitiatorsPage.FilterColumns column, List<String> strings) {
        SelenideElement filter = $x(String.format(filterTemplate, column.getName()));
        if (column.getElementType().equals(UiDropDownWithCheckBox.class)) {
            new UiDropDownWithCheckBox(filter, column.getName())
                    .clear()
                    .selectOptions(strings);
        } else if (column.getElementType().equals(UiTextBox.class)) {
            new UiTextBox(filter, column.getName()).setValue(strings.get(0));
        } else if (column.getElementType().equals(UiDatePicker.class)) {
            new UiDatePicker(filter, column.getName())
                    .setRange(LocalDate.parse(strings.get(0)), LocalDate.parse(strings.get(1)));
        }
        search.click();
        WaitingUtils.waitUntil(5, 1, 1, "Ожидание фильтрации", () ->
                search.getElement().is(interactable));
        return Selenide.page(FilterInitiators.class);
    }

    public FilterInitiators checkColumnHasSameStrings(IHasColumnDescriptor columnDescriptor,
                                                      List<String> expectedNames, SoftAssertions softly) {
        softly.assertThat(getAllStringsFromColumn(columnDescriptor))
                .hasSameElementsAs(expectedNames);
        return this;
    }

    public FilterInitiators checkColumnHasSameStringsOnPage(IHasColumnDescriptor columnDescriptor,
                                                            List<String> expectedNames, SoftAssertions softly) {
        softly.assertThat(getColumnStrings(columnDescriptor))
                .hasSameElementsAs(expectedNames);
        return this;
    }

    public List<String> getColumnStrings(IHasColumnDescriptor column){
        UiTable uiTable = new UiTable($x("//table"), "Таблица с аукционами");
        return uiTable.getColumn(column).texts();
    }

    @Step("Сохранить значения столбца со всех страниц")
    public List<String> getAllStringsFromColumn(IHasColumnDescriptor column){
        AllureEdit.removeParamByName("column");
        List<String> temp = new ArrayList<>(getColumnStrings(column));
        while (nextPage.getElement().is(interactable)) {
            nextPage.click();
            $("body").click();
            WaitingUtils.waitUntil(5, 1, 1, "Ожидание загрузки новой страницы", () ->
                    search.getElement().is(interactable)
            );
            SpvbUtils.step("Сохранить строки на текущей странице таблицы");
            temp.addAll(getColumnStrings(column));
        }
        return temp;
    }
    public FilterInitiators checkColumnHasString(IHasColumnDescriptor column, String expected, SoftAssertions softly){
        List<String> temp = getAllStringsFromColumn(column);
        for(String str : temp){
            softly.assertThat(str)
                    .contains(expected);
        }
        return this;
    }

    public FilterInitiators checkColumnDatesBetween(IHasColumnDescriptor column, LocalDateTime from, LocalDateTime to,
                                                    DateFormat format, SoftAssertions softly){
        List<String> temp = getAllStringsFromColumn(column);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format.getValue());

        for(String str : temp){
            LocalDateTime currentDateTime = LocalDateTime.parse(str, formatter);
            softly.assertThat(currentDateTime)
                    .isBetween(from, to);
        }

        return this;
    }

    public FilterInitiators checkColumnContains(IHasColumnDescriptor column, List<String> expected, SoftAssertions softly){
        List<String> temp = getAllStringsFromColumn(column);
        for(String str: temp){
            softly.assertThat(str)
                    .isIn(expected);
        }
        return this;
    }

    public FilterInitiators checkFilterEmpty(FilterColumns column){
        SelenideElement filter = $x(String.format(filterTemplate, column.getName()));
        WaitingUtils.waitUntil(5,1,1,"Ожидание сброса фильтра",()-> Objects.requireNonNull(filter.getValue()).isEmpty());
        return this;
    }

}
