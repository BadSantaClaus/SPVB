package page.web.initiators.auction;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import constants.DocStatus;
import elements.web.UiButton;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import page.web.initiators.InitiatorsPage;
import utils.SpvbUtils;

import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public abstract class OpenAucOrTradeHeaderPage extends InitiatorsPage {
    @AllArgsConstructor
    @Getter
    public enum GeneralStatus {
        NEW("Новый"),
        IN_WORK("В работе"),
        WAITING("Ожидание"),
        COLLECTING_APPLICATIONS("Сбор заявок"),
        COLLECTING_TRADES("Сбор сделок"),
        RESULTS("Итоги"),
        DECLINED("Отклонен"),
        COMPLETED("Завершен");
        private final String status;
    }
    @AllArgsConstructor
    @Getter
    public enum Section {
        AUC_PARAMS("Параметры депозитного аукциона"),
        TRADE_PARAMS("Параметры депозитных торгов"),
        APPLICATIONS("Реестр заявок"),
        TRADES("Реестр сделок"),
        NRD("НРД"),
        RESULTS("Итоги");
        private final String section;
    }
    public OpenAucOrTradeHeaderPage checkGeneralStatus(GeneralStatus status){
        Assertions.assertThat(
                        $x("//span[text()='" + status.getStatus() +"' and ancestor::" +
                                "div[preceding-sibling::div/div/h1[text()[contains(., 'Объявление')]]]]").getText())
                .as("Проверить, что статут объявления равен " + status.getStatus())
                .isEqualTo(status.getStatus());
        return this;
    }

    public OpenAucOrTradeHeaderPage checkDocStatus(DocStatus status){
        Assertions.assertThat(
                        $x("//span[text()='" + status.getValue() +"' and not (ancestor::" +
                                "div[preceding-sibling::div/div/h1[text()[contains(., 'Объявление')]]])]").getText())
                .as("Проверить, что статут документа равен " + status.getValue())
                .isEqualTo(status.getValue());
        return this;
    }

    public <T extends OpenAucOrTradeHeaderPage> T openSection(Section section, Class<T> nextPageClass){
        UiButton button = new UiButton($x("//button/span[text()='" + section.getSection() + "']"), section.getSection());
        button.click();
        SpvbUtils.takeScreenshot();
        return nextPageClass.cast(Selenide.page(nextPageClass));
    }

    public List<String> getDocs(){
        ElementsCollection collection = $$x("//span[text()[contains(.,'.')] and text()[not(contains(., 'а'))]]");
        List<String> list = new ArrayList<>();
        for (SelenideElement element : collection){
            list.add(element.getText());
        }
        return list;
    }
}
