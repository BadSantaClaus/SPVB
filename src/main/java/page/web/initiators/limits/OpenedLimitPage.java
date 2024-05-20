package page.web.initiators.limits;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import constants.DocStatus;
import elements.web.UiButton;
import elements.web.UiCheckbox;
import elements.web.UiDropdown;
import elements.web.UiTextBox;
import model.dto.YTDto;
import org.assertj.core.api.Assertions;
import page.web.initiators.InitiatorsPage;
import utils.SpvbUtils;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class OpenedLimitPage extends InitiatorsPage{
    private static OpenedLimitPage instance;
    UiDropdown tagButtonMenu = new UiDropdown( $x("//input[ancestor::div[preceding-sibling::div/div/p/label[text()='Тег расчетов']]" +
            " and @type='text']"), "Тег расчетов");
    UiButton addLimitsForYTButton = new UiButton($x("//span[text()='+ Добавить Лимиты для УТ']"), "Добавить лимиты для УТ");
    UiButton exportButton = new UiButton($x("//span[text()='Экспорт лимитов УТ в ТС']"), "Экспорт лимитов УТ в ТС");

    public static OpenedLimitPage getInstance() {
        if (instance == null) {
            instance = new OpenedLimitPage();
        }
        return instance;
    }

    public OpenedLimitPage setLotRubleCheckbox(Boolean position) {
        UiCheckbox lotRubleCheckbox = new UiCheckbox($x("//input[@type='checkbox' and not (@checked)]"),
                "Переключатель лот/рубль", $x("//label[text()='Лот']").has(Condition.exist, Duration.ofSeconds(10)));
        lotRubleCheckbox.setCheckbox(position);
        waitContentUpdate();
        return this;
    }

    public OpenedLimitPage setCalculationsTag(String tagName) {
        tagButtonMenu.selectOption(tagName);
        waitContentUpdate();
        return this;
    }

    public OpenedLimitPage setInstrumentSpec(String spec) {
        UiTextBox textBox = new UiTextBox($x("//input[ancestor::div[preceding-sibling::div/div/p/label[text()='Спецификация инструмента']]" +
                " and @type='text']"), "Спецификация инструмента");
        textBox.setValueFromDropDown(spec);
        return this;
    }

    public OpenedLimitPage addYTLimits(List<YTDto> list) {
        for (YTDto ytDto : list) {
            addLimitsForYTButton.click();
            UiTextBox name = new UiTextBox($x("//form/div/div[descendant::label[text()='Наименование УТ']]//input"),
                    "Наименование УТ");
            name.setValue(ytDto.getName());
            $x("//div[@class='MuiAutocomplete-popper']//ul/li[1]").click();
            UiButton tradeAccount = new UiButton($x("//div[@class='MuiDialogContent-root']//div[descendant::label[text()='Торговый счет УТ'] " +
                    "and (following-sibling::div[text() = 'Значение лимита для диапазона сроков:'])]//button"), "Торговый счет УТ");
            tradeAccount.click();
            $x("//div[@class='MuiAutocomplete-popper']//ul/li[1]").click();
            UiTextBox generalLimit = new UiTextBox($x("//form/div/div[descendant::label[text()='Значение общего лимита']]//input"), "Значение общего лимита");
            generalLimit.setValue(ytDto.getGeneralLimit());
            for (int i = 0; i < ytDto.getTermLimits().size(); i++) {
                UiButton plus = new UiButton($x("//button[parent::div[parent::div[preceding-sibling::div[text()='Значение лимита для диапазона сроков:']]]]")
                        , "плюс");
                plus.click();
                UiTextBox term = new UiTextBox($x("//div[.//div[text() = 'Значение лимита'] and (following-sibling::div[text() = 'Значение лимита для диапазона сроков:']" +
                        " or preceding-sibling::div[text() = 'Значение лимита для диапазона сроков:'])]//input[@name='limits[" + i + "].term']"), "Значение лимита");
                term.setValue(String.valueOf(ytDto.getTermLimits().get(i).get(0)));
                UiTextBox limit = new UiTextBox($x("//div[.//div[text() = 'Значение лимита'] and (following-sibling::div[text() = 'Значение лимита для диапазона сроков:']" +
                        " or preceding-sibling::div[text() = 'Значение лимита для диапазона сроков:'])]//input[@name='limits[" + i + "].limit']"), "Значение лимита");
                limit.setValue(String.valueOf(ytDto.getTermLimits().get(i).get(1)));
            }
            UiButton add = new UiButton($x("//button//span[text()='Добавить']"), "Добавить");
            add.click();
            waitContentUpdate();
            SpvbUtils.takeScreenshot();
        }
        return this;
    }

    public OpenedLimitPage exportLimits() {
        exportButton.click();
        waitContentUpdate();
        return this;
    }

    public OpenedLimitPage sendLimits() {
        UiButton button = new UiButton($x("//button[./span[text()='Рассылка лимитов']]"), "Рассылка лимитов");
        button.click();
        waitContentUpdate();
        SpvbUtils.takeScreenshot();
        return this;
    }

    public File downloadLimFile() {
        UiButton downloadLimFileButton = new UiButton($x("//span[text()='Скачать lim-файл']"), "Скачать lim-файл");
        return downloadLimFileButton.download();
    }

    public OpenedLimitPage documentStatus(DocStatus status){
        Assertions.assertThat(status.getValue())
                .as("Проверить, что статус документа " + status.getValue())
                .isEqualTo($x("//span[text() = '" + status.getValue() + "']").getText());
        return this;
    }

    public OpenedLimitPage dateExportToTCExists() {
        Assertions.assertThat($x("//div[text()[contains(., 'Дата экспорта в ТС')]]").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что отображается дата экспотра в ТС")
                .isTrue();
        return this;
    }

    public OpenedLimitPage dateExportToLKExists() {
        Assertions.assertThat($x("//div[text()[contains(., 'Дата экспорта в ЛК')]]").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что отображается дата экспотра в ЛК")
                .isTrue();
        return this;
    }

    private void waitContentUpdate() {
        $x("//div[contains(@class, \"MuiCircularProgress\")]").shouldNot(Condition.visible, Duration.ofSeconds(20));
    }

    public ElementsCollection getContent() {
        SelenideElement table = $x("//table");
        ElementsCollection plusButtons = $$x("//table//td[1]//button");
        for (SelenideElement button : plusButtons) {
            new UiButton(button, "Плюс").click();
        }
        return table.$$x(".//tr");
    }

    public OpenedLimitPage decline() {
        UiButton uiButton = new UiButton($x("//span[text()='Отклонить']"), "Отклонить");
        uiButton.click();
        uiButton = new UiButton($x("//div[contains(@class, 'MuiDialog')]//span[text()='Отклонить']"), "Отклонить");
        uiButton.click();
        return this;
    }

    public OpenedLimitPage exportError() {
        Assertions.assertThat($x("//p[text()=\"В данный момент обрабатывается другой документ в статусе 'Экспортирован в ТС'\"]").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что произошда ошибка 'В данный момент обрабатывается другой документ в статусе 'Экспортирован в ТС'")
                .isTrue();
        UiButton button = new UiButton($x("//span[text()='OK']"), "ОК");
        button.click();
        return this;
    }

    public OpenedLimitPage timeError(){
        Assertions.assertThat($x("//p[text()='Не удалось экспортировать лимиты в Торговую систему " +
                "так как запуск бизнес-процесса \"Экспорт лимитов\" возможно только в рамках " +
                "периодов: \"Начало дня\", \"Предторговый период\" и \"Операционный день\".']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что выдало ошибку: Не удалось экспортировать лимиты в Торговую систему \" +\n" +
                        "\"так как запуск бизнес-процесса \\\"Экспорт лимитов\\\" возможно только в рамках \" +\n" +
                        "\"периодов: \\\"Начало дня\\\", \\\"Предторговый период\\\" и \\\"Операционный день\\\".")
                .isTrue();
        SpvbUtils.takeScreenshot();
        UiButton button = new UiButton($x("//span[text()='OK']"), "ОК");
        button.click();
        return this;
    }
}
