package page.web.initiators.overCountRepo;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import constants.DocStatus;
import elements.web.UiButton;
import elements.web.UiTable;
import elements.web.UiTextBox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.assertj.core.api.SoftAssertions;
import page.web.initiators.InitiatorsPage;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

@SuppressWarnings({"UnusedReturnValue"})
public class OverCountRepoAppPage extends InitiatorsPage {

    private final String tableSelector = "//table";
    private final String fieldTemplate = "//*[text()='%s']/../../../following-sibling::*//input";
    UiTextBox paymentTerms = new UiTextBox($x(String.format(fieldTemplate, "Условия расчетов")), "Условия расчетов");

    public static OverCountRepoAppPage instance;

    @AllArgsConstructor
    @Getter
    public enum Tabs {
        SELECT_PARAMS("Параметры отбора заявок"),
        APP_REGISTRY("Реестр заявок"),
        COUNTERCLAIMS_REGISTRY("Реестр встречных заявок"),
        TRANSACTIONS_REGISTRY("Реестр сделок"),
        NRD("НРД"),
        RESULTS("Итоги");
        private final String tabName;
    }

    public static OverCountRepoAppPage getInstance() {
        if (instance == null)
            instance = new OverCountRepoAppPage();
        return instance;
    }

    public SelenideElement docStatus(){return $x("//h1/../../../following-sibling::*//*[contains(@style, 'background')]");}

    public UiButton spanButton(String text) {
        return new UiButton($x(String.format("//span[text() = '%s']/ancestor::button", text)), text);
    }

    public OverCountRepoAppPage insertPaymentTerms(String str) {
        paymentTerms.setValue(str);
        spanButton("Сохранить").click();
        return this;
    }

    public OverCountRepoAppPage checkAucStatus(DocStatus expected) {
        SelenideElement currStatus = $x("//h1/../../..//*[contains(@style, 'background')]")
                .scrollIntoView(false);
        WaitingUtils.waitUntil(30, 1, 1, "Проверить, что статус аукциона " + expected.getValue(),
                () -> currStatus.getText().equals(expected.getValue()));
        SpvbUtils.takeScreenshot();
        return this;
    }

    public OverCountRepoAppPage checkDocumentStatus(DocStatus expected) {
        docStatus().scrollIntoView(false);
        WaitingUtils.waitUntil(30, 1, 1, "Проверить, что статус аукциона " + expected.getValue(),
                () -> docStatus().getText().equals(expected.getValue()));
        SpvbUtils.takeScreenshot();
        return this;
    }

    public OverCountRepoAppPage uploadDoc(File file) {
        SpvbUtils.step("Прикрепить файл " + file.getName());
        SelenideElement input = $x("//input[@type='file']");
        input.scrollIntoView(false).uploadFile(file);
        return this;
    }

    public ElementsCollection docsOnTab(){
        return $$x("//button[contains(@class, 'MuiButton-textPrimary')] |" +
                "//span[contains(@class, 'MuiTypography-colorPrimary')]");
    }

    public OverCountRepoAppPage checkDocNames(List<String> expected) {
        List<String> currDocs = docsOnTab()
                .shouldHave(size(expected.size()), Duration.ofMinutes(5)).texts();

        SoftAssertions softly = new SoftAssertions();

        for(String exp : expected){
            boolean isExist = false;
            for(String act : currDocs){
                if(act.contains(exp)) {
                    isExist = true;
                    break;
                }
            }
            softly.assertThat(isExist)
                    .isTrue()
                    .describedAs("Проверить, что есть файл, содержащий в названии");
        }
        softly.assertAll();
        return this;
    }

    public OverCountRepoAppPage checkTableEmpty(boolean isEmpty) {
        WaitingUtils.waitUntil(5, 1, 1, "Проверить, что таблица не имеет данные",
                () -> new UiTable($x(tableSelector), "Реестр заявок").isTableEmpty()==isEmpty);
        return this;
    }

    public OverCountRepoAppPage checkTableContainsRow(Map<String, String> rowData){
        SelenideElement table = $x(tableSelector);
        List<String> headers = table.$$x(".//th").texts();
        for(Map.Entry<String, String> entry : rowData.entrySet()) {
            int columnIdx = headers.indexOf(entry.getKey());
            ElementsCollection rows = table.$$x(String.format(".//td[%s]", columnIdx+1)).should(sizeGreaterThan(0));
            WaitingUtils.waitUntil(60,1,1, "Проверить, что таблица содержит строку "+ entry.getValue(),
                    ()->rows.texts().contains(entry.getValue()));
        }
        return this;
    }

    public OverCountRepoAppPage fillResultTabFields(String totalAmount, String averageRate){
        String xpathTemplate = "//*[contains(text(),'%s')]/../../../following-sibling::*//input";
        UiTextBox amount = new UiTextBox($x(String.format(xpathTemplate, "Общий объем средств по заключенным договорам")), "Общий объем средств по заключенным договорам, млн. руб.");
        UiTextBox rate = new UiTextBox($x(String.format(xpathTemplate, "Средневзвешенная ставка по удовлетворенными Заявкам,")), "Общий объем средств по заключенным договорам, млн. руб.");
        amount.sendKeys(totalAmount);
        rate.sendKeys(averageRate);
        return this;
    }
}
