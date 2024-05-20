package page.web.initiators.auction;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import constants.DateFormat;
import elements.web.UiButton;
import elements.web.UiDropdown;
import elements.web.UiTextBox;
import org.assertj.core.api.Assertions;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

import static com.codeborne.selenide.Selenide.$x;
public class OpenedAucOrTradeParamsPage extends OpenAucOrTradeHeaderPage {
    private static OpenedAucOrTradeParamsPage instance;
    public static OpenedAucOrTradeParamsPage getInstance() {
        if (instance == null) {
            instance = new OpenedAucOrTradeParamsPage();
        }
        return instance;
    }
    //Beginning of fill block-------------------------------------------------------------------------------------------
    UiTextBox paymentTerms = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Условия расчетов']] and @type='text']"),
            "Условия расчетов");
    SelenideElement depositTradingDateButton;
    UiTextBox depositTradingDate;
    SelenideElement depositAuctionDateButton;
    UiTextBox depositAuctionDate;
    UiTextBox contractTerm = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Срок договора']] and @type='text']"),
            "Срок договора");
    UiDropdown applicationMethod = new UiDropdown($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Способ подачи заявок']] and @type='text']"),
            "Способ подачи заявок");
    UiTextBox timeFrom = new UiTextBox($x("//input[@name='input_orders_period_from']"),
            "Период ввода заявок от");
    UiTextBox timeTo = new UiTextBox($x("//input[@name='input_orders_period_to']"),
            "Период ввода заявок до");
    UiTextBox timeBetTo = new UiTextBox($x("//input[@name='rate_setting_period_to']"),
            "Период ввода заявок до");
    UiDropdown instrumentSpec = new UiDropdown($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Спецификация инструмента']] and @type='text']"),
            "Спецификация инструмента");
    UiDropdown calcTeg = new UiDropdown($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Тег расчетов']] and @type='text']"),
            "Тег расчетов");
    SelenideElement settlementDateButton = $x("//button[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Дата расчетов']]]//*[@class='MuiSvgIcon-root MuiSvgIcon-colorSecondary']");
    UiTextBox settlementDate = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Дата расчетов']] and @type='text']"),
            "Дата расчетов");

    UiTextBox endDate = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Дата окончания']] and @type='text']"),
            "Дата окончания");
    UiDropdown currencySet = new UiDropdown($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Валюта обязательств']] and @type='text']"),
            "Валюта обязательств");
    UiDropdown satisfyingApplications;
    UiTextBox minPercent = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Минимальная процентная ставка']] and @type='text']"),
            "Минимальная процентная ставка");
    UiTextBox maxSum = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Максимальная сумма размещаемых средств (в млн.)']] and @type='text']"),
            "Максимальная сумма размещаемых средств (в млн.)");
    UiDropdown typeOfDepositRate;

    public OpenedAucOrTradeParamsPage setTypeOfDepositRate(String value){
        typeOfDepositRate = new UiDropdown($x("//input[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Вид ставки де" +
                "позита']] and @type='text']"),
                "Вид ставки депозита");
        typeOfDepositRate.selectOption(value);
        return this;
    }

    private void dateToday(){
        $x("//div[text()='Сегодня']").click();
    }
    public OpenedAucOrTradeParamsPage setPaymentTerms(String value){
        paymentTerms.setValue(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setContractTerm(String value){
        contractTerm.setValue(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setDepositTradingDate(String value){
        depositTradingDate = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Дата депозитных торгов']] and @type='text']"),
                "Дата депозитных торгов");
        depositTradingDate.setValue(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setDepositTradingDateToday(){
        depositTradingDateButton = $x("//button[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Дата депозитных торгов']]]//*[@class='MuiSvgIcon-root MuiSvgIcon-colorSecondary']");
        depositTradingDateButton.click();
        dateToday();
        return this;
    }

    public OpenedAucOrTradeParamsPage setAuctionTradingDate(String value){
        depositAuctionDate = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Дата депозитных торгов']] and @type='text']"),
                "Дата депозитного аукциона");
        depositAuctionDate.setValue(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setDepositAuctionDateToday(){
        depositAuctionDateButton = $x("//button[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Дата депозитного аукциона']]]//*[@class='MuiSvgIcon-root MuiSvgIcon-colorSecondary']");
        depositAuctionDateButton.click();
        dateToday();
        return this;
    }

    public OpenedAucOrTradeParamsPage setApplicationMethod(String value){
        applicationMethod.selectOption(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setTime(String start, String end){
        timeFrom.sendKeys(start);
        timeTo.sendKeys(end);
        return this;
    }

    public OpenedAucOrTradeParamsPage setTimeBetTo(String end){
        timeBetTo.sendKeys(end);
        return this;
    }

    public OpenedAucOrTradeParamsPage setInstrumentSpec(String value){
        instrumentSpec.selectOption(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setCalcTag(String value){
        calcTeg.selectOption(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setSettlementDate(String value){
        settlementDate.setValue(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setSettlementDateToday(){
        settlementDateButton.click();
        dateToday();
        return this;
    }

    public OpenedAucOrTradeParamsPage setCurrency(String value){
        currencySet.selectOption(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setSatisfyingApplications(String value){
        satisfyingApplications = new UiDropdown($x("//input[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Способ удовлетворения заявок']] and @type='text']"),
                "Способ удовлетворения заявок");
        satisfyingApplications.selectOption(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setMinPercent(String value){
        minPercent.setValue(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setMaxSum(String value){
        maxSum.setValue(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setReturnDate(String value){
        UiTextBox returnDate = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
                "div/div/p/label[text()='Дата возврата средств']] and @type='text']"),
                "Дата возврата средств");
        returnDate.sendKeys(value);
        return this;
    }

    public OpenedAucOrTradeParamsPage setRateIncreaseTime(String from, String to){
        UiTextBox fromBox = new UiTextBox($x("//input[@name='boost_mode_period_from']"), "Время повышения ставки от");
        UiTextBox toBox = new UiTextBox($x("//input[@name='boost_mode_period_to']"), "Время повышения ставки до");
        fromBox.sendKeys(from);
        toBox.sendKeys(to);
        return this;
    }

    public OpenedAucOrTradeParamsPage changeRateIncreaseTime(String from, String to){
        UiTextBox fromBox = new UiTextBox($x("//input[@name='boost_mode_period_from']"), "Время повышения ставки от");
        UiTextBox toBox = new UiTextBox($x("//input[@name='boost_mode_period_to']"), "Время повышения ставки до");
        fromBox.clear();
        toBox.clear();
        fromBox.sendKeys(from);
        toBox.sendKeys(to);
        return this;
    }

    public OpenedAucOrTradeParamsPage changeTime(String start, String end){
        timeFrom.clear();
        timeTo.clear();
        timeFrom.sendKeys(start);
        timeTo.sendKeys(end);
        return this;
    }

    public OpenedAucOrTradeParamsPage changeTimeBetTo(String end){
        timeBetTo.clear();
        timeBetTo.sendKeys(end);
        return this;
    }

    //End of fill block-------------------------------------------------------------------------------------------------
    //Beginning of check block------------------------------------------------------------------------------------------
    UiTextBox BICode = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Идентификатор договоров банковского депозита (код БИ)']] and @type='text']"),
            "Идентификатор договоров банковского депозита (код БИ)");
    UiTextBox BIName = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Наименование БИ']] and @type='text']"),
            "Наименование БИ");
    UiTextBox typeByPrice = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Тип исполнения заявок по цене']] and @type='text']"),
            "Тип исполнения заявок по цене");
    UiTextBox typeByVolume = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Тип исполнения заявок по объему']] and @type='text']"),
            "Тип исполнения заявок по объему");
    UiTextBox priceUnit = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Единица измерения цены']] and @type='text']"),
            "Единица измерения цены");
    UiTextBox typeOfAgreement = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Вид договора']] and @type='text']"),
            "Вид договора");
    UiTextBox typeOfPrice = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Вид цены']] and @type='text']"),
            "Вид цены");
    UiTextBox priceStep = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Шаг цены']] and @type='text']"),
            "Шаг цены");
    UiTextBox priceAccuracy = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Точность цены']] and @type='text']"),
            "Точность цены");
    UiTextBox currencyCheck = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Валюта']] and @type='text']"),
            "Валюта");
    UiTextBox lot = new UiTextBox($x("//input[ancestor::div[preceding-sibling::" +
            "div/div/p/label[text()='Лот']] and @type='text']"),
            "Лот");

    public OpenedAucOrTradeParamsPage BICodeIsEqual(String value){
        Assertions.assertThat(BICode.getValue())
                .as("Проверить, что код БИ равен " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage BINameIsEqual(String value){
        Assertions.assertThat(BIName.getValue())
                .as("Проверить, что наименование БИ равно " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage typeByPriceIsEqual(String value){
        Assertions.assertThat(typeByPrice.getValue())
                .as("Проверить, что тип исполнения заявок по цене равен " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage typeByVolumeIsEqual(String value){
        Assertions.assertThat(typeByVolume.getValue())
                .as("Проверить, что тип исполнения заявок по объёму равен " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage priceUnitIsEqual(String value){
        Assertions.assertThat(priceUnit.getValue())
                .as("Проверить, что еденица измерения цены равна " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage typeOfAgreementIsEqual(String value){
        Assertions.assertThat(typeOfAgreement.getValue())
                .as("Проверить, что вид договора равен " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage typeOfPriceIsEqual(String value){
        Assertions.assertThat(typeOfPrice.getValue())
                .as("Проверить, что вид цены равен " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage priceStepIsEqual(String value){
        Assertions.assertThat(priceStep.getValue())
                .as("Проверить, что шаг цены равен " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage priceAccuracyIsEqual(String value){
        Assertions.assertThat(priceAccuracy.getValue())
                .as("Проверить, что точность цены равна " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage currencyCheckIsEqual(String value){
        Assertions.assertThat(currencyCheck.getValue())
                .as("Проверить, что валюта равна " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage lotIsEqual(String value){
        Assertions.assertThat(lot.getValue())
                .as("Проверить, что лот равен " + value)
                .isEqualTo(value);
        return this;
    }
    public OpenedAucOrTradeParamsPage BICodeIsNotEmpty(){
        Assertions.assertThat(BICode.getValue().isEmpty())
                .as("Проверить, что поле 'Идентификатор договоров банковского депозита (код БИ)' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage BINameIsNotEmpty(){
        Assertions.assertThat(BIName.getValue().isEmpty())
                .as("Проверить, что поле 'Наименование БИ' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage typeByPriceIsNotEmpty(){
        Assertions.assertThat(typeByPrice.getValue().isEmpty())
                .as("Проверить, что поле 'Тип исполнения заявок по цене' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage typeByVolumeIsNotEmpty(){
        Assertions.assertThat(typeByVolume.getValue().isEmpty())
                .as("Проверить, что поле 'Тип исполнения заявок по объему' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage priceUnitIsNotEmpty(){
        Assertions.assertThat(priceUnit.getValue().isEmpty())
                .as("Проверить, что поле 'Единица измерения цены' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage typeOfAgreementIsNotEmpty(){
        Assertions.assertThat(typeOfAgreement.getValue().isEmpty())
                .as("Проверить, что поле 'Вид договора' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage typeOfPriceIsNotEmpty(){
        Assertions.assertThat(typeOfPrice.getValue().isEmpty())
                .as("Проверить, что поле 'Вид цены' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage priceStepIsNotEmpty(){
        Assertions.assertThat(priceStep.getValue().isEmpty())
                .as("Проверить, что поле 'Шаг цены' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage priceAccuracyIsNotEmpty(){
        Assertions.assertThat(priceAccuracy.getValue().isEmpty())
                .as("Проверить, что поле 'Точность цены' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage currencyCheckIsNotEmpty(){
        Assertions.assertThat(currencyCheck.getValue().isEmpty())
                .as("Проверить, что поле 'Валюта' содержит значение")
                .isFalse();
        return this;
    }
    public OpenedAucOrTradeParamsPage lotIsNotEmpty(){
        Assertions.assertThat(lot.getValue().isEmpty())
                .as("Проверить, что поле '' содержит значение")
                .isFalse();
        return this;
    }

    public OpenedAucOrTradeParamsPage checkEndDate(){
        LocalDate date = LocalDate.parse(settlementDate.getValue(), DateTimeFormatter.ofPattern(DateFormat.WEB_DATE_FORMAT.getValue()));
        date = date.plusDays(Integer.parseInt(contractTerm.getValue()));
        Assertions.assertThat(date)
                .as("Проверить, что поле 'Дата окончания' верно сгенерировалось")
                .isEqualTo(LocalDate.parse(endDate.getValue(), DateTimeFormatter.ofPattern(DateFormat.WEB_DATE_FORMAT.getValue())));
        return this;
    }
    //End of check block------------------------------------------------------------------------------------------------

    UiButton save = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
    public OpenedAucOrTradeParamsPage save(){
        Callable check = () ->
                !calcTeg.getElement()
                        .getAttribute("value").isEmpty();
        WaitingUtils.waitUntil(3, 1, 1, "тег заполнен", check);
        save.click();
        return this;
    }

    public OpenedAucOrTradeParamsPage editExists(){
        Assertions.assertThat($x("//span[text()='Редактировать']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что кнопка 'Редактировать' отображается")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeParamsPage declineExists(){
        Assertions.assertThat($x("//span[text()='Отклонить']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что кнопка 'Отклонить' отображается")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeParamsPage exportExists(){
        Assertions.assertThat($x("//span[text()='Экспортировать аукцион в ТС']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что кнопка 'Экспортировать аукцион в ТС' отображается")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeParamsPage sendExists(){
        Assertions.assertThat($x("//span[text()='Рассылка объявления УТ']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что кнопка 'Рассылка объявления УТ' отображается")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeParamsPage export(){
        UiButton button = new UiButton($x("//span[text()='Экспортировать аукцион в ТС']"),
                "Экспортировать аукцион в ТС");
        button.click();
        return this;
    }

    public OpenedAucOrTradeParamsPage send(){
        UiButton button = new UiButton($x("//span[text()='Рассылка объявления УТ']"),
                "Рассылка объявления УТ");
        button.click();
        SpvbUtils.takeScreenshot();
        return this;
    }
    public OpenedAucOrTradeParamsPage update(){
        UiButton button = new UiButton($x("//span[text()='Обновить аукцион в ТС']"),
                "Обновить аукцион в ТС");
        button.click();
        return this;
    }

    public OpenedAucOrTradeParamsPage uploadFile(File file){
        SelenideElement inputFile = $x("//input[following-sibling::p[text()='Загрузить файл']]");
        inputFile.sendKeys(file.getAbsolutePath());
        $x("//span[text()[contains(., '" + SpvbUtils.getFileExtension(file.getAbsolutePath()) + "')]]")
                .should(Condition.exist, Duration.ofSeconds(15));
        return this;
    }

    public OpenedAucOrTradeParamsPage edit(){
        UiButton edit = new UiButton($x("//span[text()='Редактировать']"), "Редактировать");
        edit.click();
        return this;
    }
}
