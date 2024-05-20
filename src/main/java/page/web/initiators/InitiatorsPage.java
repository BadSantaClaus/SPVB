package page.web.initiators;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import elements.web.UiDatePicker;
import elements.web.UiDropDownWithCheckBox;
import elements.web.UiTextBox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import page.web.BasePage;
import utils.SpvbUtils;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

@Slf4j
@SuppressWarnings({"UnusedReturnValue"})
public class InitiatorsPage extends BasePage {

    public static InitiatorsPage instance;

    public static InitiatorsPage getInstance(){
        if(instance==null)
            instance = new InitiatorsPage();
        return instance;
    }

    @AllArgsConstructor
    @Getter
    public enum UiInitiatorsSections {
        ROUTINE_OPERATIONS("Регламентные операции"),
        LIMITS("Лимиты"),
        DEPOSIT_AUCTION_N_TRADES("Депозитные аукционы/торги"),
        DOC_REGISTRY("Реестр документов"),
        OVER_COUNT_REPO("Внебиржевое РЕПО");
        private final String sectionName;
    }

    @AllArgsConstructor
    @Getter
    public enum FilterColumns{
        NAME_DOC("Имя документа", UiTextBox.class),
        EXCHANGE_INSTRUMENT("Биржевой инструмент", UiTextBox.class),
        DATE("Дата", UiDatePicker.class),
        STATUS("Статус", UiDropDownWithCheckBox.class),
        INITIATOR_CONTRIBUTOR_CODE("Код инициатора/вкладчика", UiDropDownWithCheckBox.class),
        TYPE("Тип", UiDropDownWithCheckBox.class);
        private final String name;
        private final Class<?> elementType;
    }

    public <T extends InitiatorsPage> T openSection(UiInitiatorsSections section, Class<T> nextPageClass){
        SelenideElement leftPanelModule = $x(String.format("//span[text()='%s']", section.getSectionName()));
        leftPanelModule.scrollIntoView(true).click();
        log.info("Открыть банковский модуль {}", section.getSectionName());
        SpvbUtils.takeScreenshot();
        return nextPageClass.cast(Selenide.page(nextPageClass));
    }

    public void waitLimitsLoad(){
        $x("//div[@class[contains(.,'MuiCircularProgress')]]").shouldNot(Condition.visible, Duration.ofSeconds(60));
    }
}
