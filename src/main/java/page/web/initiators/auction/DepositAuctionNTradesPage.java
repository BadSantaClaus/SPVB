package page.web.initiators.auction;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import db.initiators.auctions.AuctionInitDbHelper;
import elements.web.UiButton;
import elements.web.UiTextBox;
import io.qameta.allure.Step;
import lombok.AllArgsConstructor;
import lombok.Getter;
import page.web.initiators.InitiatorsPage;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.Callable;

import static com.codeborne.selenide.Selenide.$x;

@SuppressWarnings({"UnusedReturnValue"})
public class DepositAuctionNTradesPage extends InitiatorsPage {

    @AllArgsConstructor
    @Getter
    public enum UiAuctionsNTradeSection {
        STANDARD("Стандарт. аукционы/торги"),
        KFSPB("КФ СПБ"),
        FSKMSB("ФСКМСБ"),
        KFLO("КФ ЛО"),
        FAUGGE("ФАУ ГГЭ"),
        FK("ФК"),
        VEBRF("ВЭБ РФ");
        private final String sectionName;
    }

    @AllArgsConstructor
    @Getter
    public enum TradeType {
        AUCTION("Аукцион"),
        INITIATOR_AGAINST_ALL("Инициатор против всех");
        private final String tradesType;
    }


    public static DepositAuctionNTradesPage instance;
    UiButton auction = new UiButton($x("//button/span[text()='Аукцион']"), "Аукцион");

    UiButton auctionConditions = new UiButton($x("//span[text()='Условия проведения аукциона']"), "Условия проведения аукциона");


    public static DepositAuctionNTradesPage getInstance() {
        if (instance == null) {
            instance = new DepositAuctionNTradesPage();
        }
        return instance;
    }

    SelenideElement menu = $x("//div[child::div[@role='button']/span and preceding-sibling::div/div/*[text()='Депозитные аукционы/торги']]");

    private void waitAucTradeLoad() {
        $x("//div[@class[contains(.,'MuiCircularProgress')]]").shouldNot(Condition.visible, Duration.ofSeconds(20));
    }

    private void waitAucTradeUpload(String aucTradeName) {
        $x("//table//tr/td/button[text()='" + aucTradeName + "']").should(Condition.exist, Duration.ofSeconds(20));
    }

    @Step("Выставить инициатора {initiator}")
    private void setInitiator(String initiator) {
        UiTextBox uiTextBox = new UiTextBox($x("//input[ancestor::div[preceding-sibling::div/div/p/label[text()='Инициатор']]and @type='text']"), "Инициатор");
        uiTextBox.setValueFromDropDown(initiator);
    }

    @Step("Прикрепить файл")
    private void uploadFile(File file) {
        SelenideElement inputFile = $x("//input[following-sibling::p[text()='Загрузить файл']]");
        Callable check = () ->
                !$x("//input[ancestor::div[preceding-sibling::div/div/p/label[text()='Инициатор']]and @type='text']")
                        .getAttribute("value").isEmpty();
        WaitingUtils.waitUntil(10, 1, 1, "Поле инициатор заполнено", check);
        inputFile.sendKeys(file.getAbsolutePath());
    }

    private void createTrade(String initiator, TradeType tradeType) {
        UiButton trades = new UiButton($x("//button/span[text()='Торги']"), "Торги");
        trades.click();
        setInitiator(initiator);
        UiButton trade = new UiButton($x("//fieldset[parent::div/label[text()='Торги в режиме']]//span[text()='" + tradeType.tradesType + "']"),
                tradeType.tradesType);
        trade.click();
    }

    private void createTrade(TradeType tradeType) {
        UiButton trades = new UiButton($x("//button/span[text()='Торги']"), "Торги");
        trades.click();
        UiButton trade = new UiButton($x("//fieldset[parent::div/label[text()='Торги в режиме']]//span[text()='" + tradeType.tradesType + "']"),
                tradeType.tradesType);
        trade.click();
    }

    public DepositAuctionNTradesPage createTrades(String initiator, TradeType tradeType) {
        createTrade(initiator, tradeType);
        UiButton save = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
        save.click();
        return Selenide.page(DepositAuctionNTradesPage.class);
    }

    public DepositAuctionNTradesPage createTrades(TradeType tradeType) {
        createTrade(tradeType);
        UiButton save = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
        save.click();
        return Selenide.page(DepositAuctionNTradesPage.class);
    }

    public DepositAuctionNTradesPage createTrades(String initiator, TradeType tradeType, File file) {
        createTrade(initiator, tradeType);
        UiButton uploadCheck = new UiButton($x("//input[ancestor::div[preceding-sibling::div//label[text()='Загрузить файл объявления']]]"),
                "Загрузить файл объявления");
        WaitingUtils.sleep(3);
        uploadCheck.click();
        uploadFile(file);
        UiButton save = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
        save.click();
        waitAucTradeUpload(new AuctionInitDbHelper().getLastName(SpvbUtils.getFileExtension(file.getAbsolutePath()),
                AuctionInitDbHelper.Type.TRADE));
        return this;
    }

    public DepositAuctionNTradesPage createTrades(TradeType tradeType, File file) {
        createTrade(tradeType);
        UiButton uploadCheck = new UiButton($x("//input[ancestor::div[preceding-sibling::div//label[text()='Загрузить файл объявления']]]"),
                "Загрузить файл объявления");
        WaitingUtils.sleep(3);
        uploadCheck.click();
        uploadFile(file);
        UiButton save = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
        save.click();
        waitAucTradeUpload(new AuctionInitDbHelper().getLastName(SpvbUtils.getFileExtension(file.getAbsolutePath()),
                AuctionInitDbHelper.Type.TRADE));
        return this;
    }

    public DepositAuctionNTradesPage createAuction(String initiator, File file) {
        auction.click();
        setInitiator(initiator);
        uploadFile(file);
        UiButton save = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
        save.click();
        return this;
    }

    public DepositAuctionNTradesPage createAuction(File file) {
        auction.click();
        uploadFile(file);
        UiButton save = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
        save.click();
        return this;
    }

    public OpenedAucOrTradeParamsPage openTradePageOrAuc(String name){
        waitAucTradeLoad();
        $x("//table//tr/td/button[text()='" + name + "']").click();
        waitAucTradeLoad();
        SpvbUtils.takeScreenshot();
        return Selenide.page(OpenedAucOrTradeParamsPage.class);
    }

    public DepositAuctionNTradesPage openSection(UiAuctionsNTradeSection section) {
        UiButton button = new UiButton(menu.$x("./div[descendant::*[text()='" + section.getSectionName() + "']]"), section.getSectionName());
        button.click();
        SpvbUtils.takeScreenshot();
        return this;
    }

    public DepositAuctionNTradesPage setMinSumCon(String value){
        auctionConditions.click();
        UiTextBox textBox = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Минимальная сумма в заявке УТ']] and @type='text']"), "Минимальная сумма в заявке УТ");
        textBox.setValue(value);
        $x("//span[text()='Сохранить']").click();
        return this;
    }

    public DepositAuctionNTradesPage setMaxAplCon(String value){
        auctionConditions.click();
        UiTextBox textBox = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Максимальное количество заявок от 1 участника']] and @type='text']"),
                "Максимальное количество заявок от 1 участника");
        textBox.setValue(value);
        $x("//span[text()='Сохранить']").click();
        return this;
    }
}
