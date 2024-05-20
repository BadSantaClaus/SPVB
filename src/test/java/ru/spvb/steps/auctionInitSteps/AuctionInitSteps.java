package ru.spvb.steps.auctionInitSteps;

import com.codeborne.selenide.*;
import config.DesktopConfiguration;
import constants.Credentials;
import constants.*;
import db.DbHelper;
import db.auction.MkrDbHelper;
import elements.columns.AuctionInitStandardColumn;
import elements.columns.OverCountRepoColumn;
import elements.web.UiButton;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import model.dto.ApplicationDto;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.dom4j.Document;
import org.dom4j.tree.DefaultAttribute;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import page.quik.CurrentTradesPage;
import page.quik.RequestPage;
import page.web.AuthorizationWebPage;
import page.web.BasePage;
import page.web.initiators.InitiatorsPage;
import page.web.initiators.auction.*;
import page.web.initiators.overCountRepo.OverCountRepoAppPage;
import page.web.initiators.routineOperations.InitRoutineOperationsPage;
import ru.spvb.steps.FilterSteps;
import ru.spvb.steps.auction.AuctionSteps;
import utils.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;
import static constants.DateFormat.XML_DATE_FORMAT;
import static constants.FilePath.INITIATORS_MKR_SECURITIES;
import static constants.FilePath.TEMP_FILES;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.openqa.selenium.Keys.CONTROL;
import static org.openqa.selenium.Keys.END;
import static page.web.initiators.InitiatorsPage.FilterColumns.*;
import static page.web.initiators.InitiatorsPage.UiInitiatorsSections.DEPOSIT_AUCTION_N_TRADES;
import static page.web.initiators.InitiatorsPage.UiInitiatorsSections.DOC_REGISTRY;
import static page.web.initiators.auction.OpenAucOrTradeHeaderPage.GeneralStatus.DECLINED;

@SuppressWarnings({"UnusedReturnValue"})
public class AuctionInitSteps {

    @Step("Зайти на сайт, авторизоваться и перейти в секцию 'Депозитные аукционы/торги'")
    public AuctionInitSteps openSiteGoToInitAuc() {
        new AuthorizationWebPage().loginUser();
        new BasePage().open(MenuTab.INITIATORS, InitiatorsPage.class);
        new InitiatorsPage().openSection(DEPOSIT_AUCTION_N_TRADES, DepositAuctionNTradesPage.class);
        return this;
    }

    @Step("Зайти на сайт, авторизоваться и перейти в секцию 'Реестр документов'")
    public AuctionInitSteps openSiteGoToDocRegistry() {
        openSiteGoToInitAuc();
        InitiatorsPage.getInstance().openSection(DOC_REGISTRY, DocRegistryPage.class);
        return this;
    }

    @Step("Перейти в секцию {section}")
    public AuctionInitSteps openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection section) {
        DepositAuctionNTradesPage.getInstance().openSection(section);
        return this;
    }

    @Step("Создать торги")
    public AuctionInitSteps createTrade(DepositAuctionNTradesPage.TradeType tradeType) {
        DepositAuctionNTradesPage.getInstance().createTrades(tradeType);
        return this;
    }

    @Step("Создать торги")
    public AuctionInitSteps createTrade(String initiator, DepositAuctionNTradesPage.TradeType tradeType, File file) {
        DepositAuctionNTradesPage.getInstance().createTrades(initiator, tradeType, file);
        return this;
    }

    @Step("Создать торги")
    public AuctionInitSteps createTrade(DepositAuctionNTradesPage.TradeType tradeType, File file) {
        DepositAuctionNTradesPage.getInstance().createTrades(tradeType, file);
        return this;
    }

    @Step("Аукцион")
    public AuctionInitSteps createAuc(String initiator, File file) {
        DepositAuctionNTradesPage.getInstance().createAuction(initiator, file);
        return this;
    }

    @Step("Аукцион")
    public AuctionInitSteps createAuc(File file) {
        DepositAuctionNTradesPage.getInstance().createAuction(file);
        return this;
    }

    @Step("Заполнить все поля аукциона")
    public AuctionInitSteps fillAuc(String paymentTerms, String maxSum, String depositAuctionDate,
                                    String contractTerm, String applicationMethod,
                                    String timeFrom, String timeTo, String timeBetTo, String instrumentSpec,
                                    String calcTag, String settlementDate, String currency, String minPercent) {
        if (settlementDate.equals("today")) {
            OpenedAucOrTradeParamsPage.getInstance().setSettlementDateToday();
        } else {
            OpenedAucOrTradeParamsPage.getInstance().setSettlementDate(settlementDate);
        }
        if (depositAuctionDate.equals("today")) {
            OpenedAucOrTradeParamsPage.getInstance().setDepositAuctionDateToday();
        } else {
            OpenedAucOrTradeParamsPage.getInstance().setAuctionTradingDate(depositAuctionDate);
        }
        OpenedAucOrTradeParamsPage.getInstance()
                .setPaymentTerms(paymentTerms)
                .setMaxSum(maxSum)
                .setContractTerm(contractTerm)
                .setApplicationMethod(applicationMethod)
                .setTime(timeFrom.replace(":", ""), timeTo.replace(":", ""))
                .setTimeBetTo(timeBetTo.replace(":", ""))
                .setInstrumentSpec(instrumentSpec)
                .setCalcTag(calcTag)
                .setCurrency(currency)
                .setMinPercent(minPercent);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Заполнить все поля аукциона")
    public AuctionInitSteps fillAucNoCur(String paymentTerms, String maxSum, String depositAuctionDate,
                                         String contractTerm, String applicationMethod,
                                         String timeFrom, String timeTo, String timeBetTo, String instrumentSpec,
                                         String calcTag, String settlementDate, String minPercent) {
        if (settlementDate.equals("today")) {
            OpenedAucOrTradeParamsPage.getInstance().setSettlementDateToday();
        } else {
            OpenedAucOrTradeParamsPage.getInstance().setSettlementDate(settlementDate);
        }
        if (depositAuctionDate.equals("today")) {
            OpenedAucOrTradeParamsPage.getInstance().setDepositAuctionDateToday();
        } else {
            OpenedAucOrTradeParamsPage.getInstance().setAuctionTradingDate(depositAuctionDate);
        }
        OpenedAucOrTradeParamsPage.getInstance()
                .setPaymentTerms(paymentTerms)
                .setMaxSum(maxSum)
                .setContractTerm(contractTerm)
                .setApplicationMethod(applicationMethod)
                .setTime(timeFrom.replace(":", ""), timeTo.replace(":", ""))
                .setTimeBetTo(timeBetTo.replace(":", ""))
                .setInstrumentSpec(instrumentSpec)
                .setCalcTag(calcTag)
                .setMinPercent(minPercent);
        SpvbUtils.takeScreenshot();
        return new AuctionInitSteps();
    }

    @Step("Заполнить все поля торгов")
    public AuctionInitSteps fillTrade(String paymentTerms, String depositTradingDate,
                                      String contractTerm, String applicationMethod, String typeOfDepositRate,
                                      String timeFrom, String timeTo, String timeBetTo, String instrumentSpec,
                                      String calcTag, String settlementDate, String currency,
                                      String satisfyingApplications, String minPercent,
                                      String maxSum) {
        if (depositTradingDate.equals("today")) {
            OpenedAucOrTradeParamsPage.getInstance().setDepositTradingDateToday();
        } else {
            OpenedAucOrTradeParamsPage.getInstance().setDepositTradingDate(depositTradingDate);
        }
        if (settlementDate.equals("today")) {
            OpenedAucOrTradeParamsPage.getInstance().setSettlementDateToday();
        } else {
            OpenedAucOrTradeParamsPage.getInstance().setSettlementDate(settlementDate);
        }
        OpenedAucOrTradeParamsPage.getInstance()
                .setPaymentTerms(paymentTerms)
                .setMaxSum(maxSum)
                .setContractTerm(contractTerm)
                .setApplicationMethod(applicationMethod)
                .setTime(timeFrom.replace(":", ""), timeTo.replace(":", ""))
                .setTimeBetTo(timeBetTo.replace(":", ""))
                .setInstrumentSpec(instrumentSpec)
                .setCurrency(currency)
                .setMinPercent(minPercent)
                .setTypeOfDepositRate(typeOfDepositRate)
                .setSatisfyingApplications(satisfyingApplications)
                .setCalcTag(calcTag);
        SpvbUtils.takeScreenshot();
        return new AuctionInitSteps();
    }

    public AuctionInitSteps fillInitAgainstAll(String contractTerm, String timeFrom, String timeTo,
                                               String instrumentSpec, String calcTag, String currency, String maxSum){
        new OpenedAucOrTradeParamsPage()
                .setContractTerm(contractTerm)
                .setTime(timeFrom, timeTo)
                .setInstrumentSpec(instrumentSpec)
                .setCalcTag(calcTag)
                .setCurrency(currency)
                .setMaxSum(maxSum);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Заполнить все поля торгов")
    public AuctionInitSteps fillTrade(String timeFrom, String timeTo, String timeBetTo) {
        OpenedAucOrTradeParamsPage.getInstance()
                .setTime(timeFrom.replace(":", ""), timeTo.replace(":", ""))
                .setTimeBetTo(timeBetTo.replace(":", ""));
        SpvbUtils.takeScreenshot();
        return new AuctionInitSteps();
    }

    @Step("Проверить, что снизу поднятулись значения")
    public AuctionInitSteps checkAutofill(String code) {
        OpenedAucOrTradeParamsPage.getInstance()
                .BICodeIsEqual(code)
                .BINameIsNotEmpty()
                .typeByPriceIsNotEmpty()
                .typeByVolumeIsNotEmpty()
                .priceUnitIsNotEmpty()
                .typeOfAgreementIsNotEmpty()
                .typeOfPriceIsNotEmpty()
                .priceStepIsNotEmpty()
                .priceAccuracyIsNotEmpty()
                .currencyCheckIsNotEmpty()
                .lotIsNotEmpty();
        return this;
    }

    @Step("Открыть страницу аукциона/торгов")
    public AuctionInitSteps openTradeOrAuc(String name) {
        DepositAuctionNTradesPage.getInstance().openTradePageOrAuc(name);
        return this;
    }

    @Step("Сохранить аукцион/торги")
    public AuctionInitSteps saveParams() {
        OpenedAucOrTradeParamsPage.getInstance().save();
        return this;
    }

    @Step("Обновить аукцион в тс")
    public AuctionInitSteps updateParams() {
        OpenedAucOrTradeParamsPage.getInstance().update();
        return this;
    }

    @Step("Проверить, что существуют опции: 'Редактировать', 'Экспортировать в ТС', 'Отклонить'")
    public AuctionInitSteps exportEditDeclineExists() {
        OpenedAucOrTradeParamsPage.getInstance()
                .editExists()
                .exportExists()
                .declineExists();
        return this;
    }

    @Step("Экспортировать аукцион/торги в ТС")
    public AuctionInitSteps export() {
        OpenedAucOrTradeParamsPage.getInstance().export();
        return this;
    }

    @Step("Разослать объявления УТ")
    public AuctionInitSteps send() {
        OpenedAucOrTradeParamsPage.getInstance().send();
        return this;
    }

    @Step("Проверить статус аукциона/торгов")
    public AuctionInitSteps checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus status) {
        scrollTop();
        OpenedAucOrTradeParamsPage.getInstance().checkGeneralStatus(status);
        return this;
    }

    public void scrollTop() {
        new Actions(WebDriverRunner.getWebDriver()).keyDown(Keys.HOME).keyUp(Keys.HOME).perform();
    }

    @Step("Проверить статус документа")
    public AuctionInitSteps checkDocumentStatus(DocStatus status) {
        scrollTop();
        OpenedAucOrTradeParamsPage.getInstance().checkDocStatus(status);
        return this;
    }

    @Step("Проверить, что кнопка 'Загрузить файл' отображается")
    public AuctionInitSteps checkUploadFileExists() {
        OpenedAurOrTradeResultsPage.getInstance().uploadExists();
        return this;
    }

    @Step("Подождать до {time}")
    public AuctionInitSteps waitTime(LocalDateTime time) {
        WaitingUtils.waitUntil(time);
        return this;
    }

    @Step("Открыть секцию {section}")
    public AuctionInitSteps openSection(OpenAucOrTradeHeaderPage.Section section, OpenAucOrTradeHeaderPage page) {
        switch (section) {
            case AUC_PARAMS -> page.openSection(section, OpenedAucOrTradeParamsPage.class);
            case TRADE_PARAMS -> page.openSection(section, OpenedAucOrTradeParamsPage.class);
            case APPLICATIONS -> page.openSection(section, OpenedAucOrTradeApplicationsPage.class);
            case TRADES -> page.openSection(section, OpenedAucOrTradeTradesPage.class);
            case NRD -> page.openSection(section, OpenedAucOrTradeNRDPage.class);
            case RESULTS -> page.openSection(section, OpenedAurOrTradeResultsPage.class);
        }
        return this;
    }

    @Step("Проверить, что нет заявок")
    public AuctionInitSteps hasNoApplications() {
        OpenedAucOrTradeApplicationsPage.getInstance().noData();
        return this;
    }

    @Step("Проверить, что кнопка 'Отправить' отображается")
    public AuctionInitSteps hasSendApplicationsButton() {
        OpenedAucOrTradeApplicationsPage.getInstance().sendExists();
        return this;
    }

    @Step("Отправить заявки, выбрав все 4 формата")
    public AuctionInitSteps sendApplicationsAllFormats() {
        OpenedAucOrTradeApplicationsPage.getInstance().send()
                .allFormatsAvailable()
                .confirmSend();
        return this;
    }

    @Step("Отправить заявки")
    public AuctionInitSteps sendApplications() {
        OpenedAucOrTradeApplicationsPage.getInstance().send();
        return this;
    }

    @Step("Проверить, что прикреплённые файлы сгенерировались верно")
    public AuctionInitSteps checkParamsDocGen(int count, Pattern pattern) {
        Callable check = () ->
                !$$x("//div[following-sibling::div/div/div/span[text()='В работе']]//*[text()]").isEmpty();
        WaitingUtils.waitUntil(25, 1, 1, "отчёт отображается", check);
        List<String> list = OpenedAucOrTradeParamsPage.getInstance().getDocs();
        checkDocGen(list, count, pattern);
        return this;
    }

    @Step("Проверить, что отчёты по заявкам сгенерировались верно")
    public AuctionInitSteps checkApplicationDocGen(int count, Pattern pattern) {
        Callable check = () ->
                !$$x("//div[following-sibling::div/div/div/span[text()='Отправлен']]//*[text()]").isEmpty();
        WaitingUtils.waitUntil(25, 1, 1, "отчёт отображается", check);
        List<String> list = OpenedAucOrTradeApplicationsPage.getInstance().getDocs();
        checkDocGen(list, count, pattern);
        return this;
    }

    @Step("Проверить, что отчёты по сделкам сгенерировались верно")
    public AuctionInitSteps checkTradesDocGen(int count, Pattern pattern) {
        Callable check = () ->
                !$$x("//div[following-sibling::div/div/div/span[text()='Отправлен']]//*[text()]").isEmpty();
        WaitingUtils.waitUntil(25, 1, 1, "отчёт отображается", check);
        List<String> list = OpenedAucOrTradeTradesPage.getInstance().getDocs();
        checkDocGen(list, count, pattern);
        return this;
    }

    @Step("Проверить, что отчёты по итогам сгенерировались верно")
    public AuctionInitSteps checkResultDocGen(int count, Pattern pattern) {
        Callable check = () ->
                !$$x("//div[following-sibling::div/div/div/span[text()='В работе']]//*[text()]").isEmpty();
        WaitingUtils.waitUntil(25, 1, 1, "отчёт отображается", check);
        ElementsCollection elements = $$x("//div[following-sibling::div/div/div/span[text()='В работе']]//*[text()]");
        List<String> list = new ArrayList<>();
        for (SelenideElement element : elements) {
            list.add(element.getText());
        }
        checkDocGen(list, count, pattern);
        return this;
    }

    private void checkDocGen(List<String> list, int count, Pattern pattern) {
        Predicate<String> predicate = i -> {
            Matcher matcher = pattern.matcher(i);
            return matcher.matches();
        };
        Assertions.assertThat(list.size())
                .as("Проверить, что сгенерировалось " + count + " отчёта: " + list)
                .isEqualTo(count);
        Assertions.assertThat(list.stream().allMatch(predicate))
                .as("Проверить, что все отчёты сгенерировались по маске " + pattern.toString() + ": " + list)
                .isTrue();
    }

    @Step("Проверить, что кнопка 'Отправить' отображается")
    public AuctionInitSteps hasSendTradesButton() {
        OpenedAucOrTradeTradesPage.getInstance().sendExists();
        return this;
    }

    @Step("Отправить сделки, выбрав все 4 формата")
    public AuctionInitSteps sendTradesAllFormats() {
        OpenedAucOrTradeTradesPage.getInstance().send()
                .allFormatsAvailable()
                .confirmSend();
        return this;
    }

    public AuctionInitSteps sendAuctionResults() {
        OpenedAurOrTradeResultsPage.getInstance().send();
        return this;
    }

    @Step("Проверить, что процесс '{processName}' завершен на вкладке Регламентные операции")
    public AuctionInitSteps checkLastProcessFinishedInRoutineOps(String processName) {
        InitiatorsPage.getInstance()
                .openSection(InitiatorsPage.UiInitiatorsSections.ROUTINE_OPERATIONS, InitRoutineOperationsPage.class)
                .filterProcess(processName)
                .checkLastProcessFinished();
        return this;
    }

    @Step("Отправить сделки")
    public AuctionInitSteps sendTrades() {
        OpenedAucOrTradeTradesPage.getInstance().send();
        return this;
    }

    @Step("Получить сделки")
    public AuctionInitSteps getTrades() {
        OpenedAucOrTradeTradesPage.getInstance().get();
        return this;
    }

    @Step("Кнопка 'Обновить' заменила кнопку 'Получить'")
    public AuctionInitSteps getChangedToUpdate() {
        OpenedAucOrTradeTradesPage.getInstance().getChangedToUpdate();
        return this;
    }

    @Step("Проверить, что сделок нет")
    public AuctionInitSteps hasNoTrades() {
        OpenedAucOrTradeTradesPage.getInstance().noData();
        return this;
    }

    @Step("Загрузить файл")
    public AuctionInitSteps uploadFileResult(File file) {
        OpenedAurOrTradeResultsPage.getInstance().uploadFile(file);
        return this;
    }

    @Step("Проверить, что поле 'Причина признания депозитного аукциона несостоявшимся' доступно" +
            "для заполнения")
    public AuctionInitSteps checkInvalidAucArea() {
        OpenedAurOrTradeResultsPage.getInstance().invalidAucReasonAreaUnlocked();
        return this;
    }

    @Step("Проверить, что поле 'Фактический объем размещения средств на текущий процентный период' доступно для заполнения")
    public AuctionInitSteps checkRealVolumeAccess() {
        OpenedAurOrTradeResultsPage.getInstance().checkRealVolumeAccess();
        return this;
    }

    @Step("Проверить, что поле 'Средневзвешенная ставка депозита по удовлетворенными заявкам' доступно для заполнения")
    public AuctionInitSteps checkAverageRateAccess() {
        OpenedAurOrTradeResultsPage.getInstance().checkAverageRateAccess();
        return this;
    }

    public AuctionInitSteps fillRealVolumeAndAverageRates(String realVolume, String averageRate) {
        OpenedAurOrTradeResultsPage.getInstance().fillRealVolume(realVolume);
        OpenedAurOrTradeResultsPage.getInstance().fillAverageRate(averageRate);
        return this;
    }

    @Step("Заполнить поле 'Причина признания депозитного аукциона несостоявшимся'")
    public AuctionInitSteps fillInvalidAucReason(String reason) {
        OpenedAurOrTradeResultsPage.getInstance().fillInvalidAucReason(reason);
        return this;
    }

    @Step("Отклонить объявление")
    public AuctionInitSteps decline() {
        OpenedAurOrTradeResultsPage.getInstance().decline();
        return this;
    }

    @Step("Сгенерировать xml файл")
    @SneakyThrows
    public File generateXmlSpb_T268() {
        String id = RandomUtils.getRandomIdWithLength(12);
        String s = "<?xml version=\"1.0\" encoding=\"WINDOWS-1251\"?>\n" +
                "<SPCEX_DOC>\n" +
                "<DOC_REQUISITIONS DOC_DATE=\"2023-08-08\" DOC_TIME=\"17:27:01\" SENDER_ID=\"550\" RECEIVER_ID=\"263\" DOC_NO=\"123456\" REMARKS=\"Порядковый номер отбора 297\"/>\n" +
                "<KDX10 VER=\"1.0\">\n" +
                "<KDX10_REC " +
                "AUCTION_ID=\"" + id + "\" " +
                "SECURITYID=\"DK1000K023R\" " +
                "AUCTION_DATE=\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "FIRST_DATE=\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "SECOND_DATE=\"" + LocalDateTime.now().plusDays(23).format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "TERM=\"23\" CURR=\"RUB\" " +
                "MIN_RATE=\"0.1299\" " +
                "MAX_VALUE=\"100000000\" " +
                "FORM=\"O\" " +
                "CONDITION=\"P\" " +
                "MAX_NUMBER=\"35\" " +
                "MIN_VALUE=\"30000\" " +
                "START_TIME_1=\"" + LocalDateTime.now().plusMinutes(3).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_1=\"" + LocalDateTime.now().plusMinutes(8).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "MIDDLE_TIME_1=\"" + LocalDateTime.now().plusMinutes(6).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_2=\"" + LocalDateTime.now().plusMinutes(11).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_3=\"" + LocalDateTime.now().plusMinutes(14).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "RATE_TYPE=\"FLOATING\"/>\n" +
                "</KDX10>\n" +
                "</SPCEX_DOC>";
        File file = File.createTempFile("test268_", ".xml");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(s);
        fileWriter.close();
        return file;
    }

    @Step("Сгенерировать xml файл")
    @SneakyThrows
    public File generateXmlSpb_T287() {
        String s = "<?xml version=\"1.0\" encoding=\"WINDOWS-1251\"?>\n" +
                "<SPCEX_DOC>\n" +
                "<DOC_REQUISITIONS DOC_DATE=\"2023-08-08\" DOC_TIME=\"17:27:01\" SENDER_ID=\"550\" RECEIVER_ID=\"263\" DOC_NO=\"123456\" REMARKS=\"Порядковый номер отбора 297\"/>\n" +
                "<KDX10 VER=\"1.0\">\n" +
                "<KDX10_REC " +
                "AUCTION_ID=\"" + RandomUtils.getRandomNumWithLength(12) + "\" " +
                "SECURITYID=\"DK1000K023R\" " +
                "AUCTION_DATE=\"" + LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "FIRST_DATE=\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "SECOND_DATE=\"" + LocalDateTime.now().plusDays(23).format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "TERM=\"23\" CURR=\"RUB\" " +
                "MIN_RATE=\"0.1299\" " +
                "MAX_VALUE=\"100000000\" " +
                "FORM=\"O\" " +
                "CONDITION=\"P\" " +
                "MAX_NUMBER=\"35\" " +
                "MIN_VALUE=\"30000\" " +
                "START_TIME_1=\"" + LocalDateTime.now().plusMinutes(3).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_1=\"" + LocalDateTime.now().plusMinutes(8).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "MIDDLE_TIME_1=\"" + LocalDateTime.now().plusMinutes(6).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_2=\"" + LocalDateTime.now().plusMinutes(11).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_3=\"" + LocalDateTime.now().plusMinutes(14).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "RATE_TYPE=\"FLOATING\"/>\n" +
                "</KDX10>\n" +
                "</SPCEX_DOC>";
        File file = File.createTempFile("test287_", ".xml");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(s);
        fileWriter.close();
        return file;
    }

    @Step("Сгенерировать xml файл")
    @SneakyThrows
    public File generateXmlSpb_T269() {
        String id = RandomUtils.getRandomIdWithLength(12);
        String s = "<?xml version=\"1.0\" encoding=\"WINDOWS-1251\"?>\n" +
                "<SPCEX_DOC>\n" +
                "<DOC_REQUISITIONS DOC_DATE=\"2023-08-08\" DOC_TIME=\"17:27:01\" SENDER_ID=\"550\" RECEIVER_ID=\"263\" DOC_NO=\"123456\" REMARKS=\"Порядковый номер отбора 297\"/>\n" +
                "<KDX10 VER=\"1.0\">\n" +
                "<KDX10_REC " +
                "AUCTION_ID=\"" + id + "\" " +
                "SECURITYID=\"DK1000K005R\" " +
                "AUCTION_DATE=\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "FIRST_DATE=\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "SECOND_DATE=\"" + LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "TERM=\"5\" CURR=\"RUB\" " +
                "MIN_RATE=\"0.1299\" " +
                "MAX_VALUE=\"100000000\" " +
                "FORM=\"O\" " +
                "CONDITION=\"P\" " +
                "MAX_NUMBER=\"35\" " +
                "MIN_VALUE=\"30000\" " +
                "START_TIME_1=\"" + LocalDateTime.now().plusMinutes(3).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_1=\"" + LocalDateTime.now().plusMinutes(8).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "MIDDLE_TIME_1=\"" + LocalDateTime.now().plusMinutes(6).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_2=\"" + LocalDateTime.now().plusMinutes(11).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_3=\"" + LocalDateTime.now().plusMinutes(14).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "RATE_TYPE=\"FLOATING\"/>\n" +
                "</KDX10>\n" +
                "</SPCEX_DOC>";
        File file = File.createTempFile("test268_", ".xml");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(s);
        fileWriter.close();
        return file;
    }

    @Step("Сгенерировать xml файл")
    @SneakyThrows
    public File generateXmlSpb_T270() {
        String id = RandomUtils.getRandomIdWithLength(12);
        String s = "<?xml version=\"1.0\" encoding=\"WINDOWS-1251\"?>\n" +
                "<SPCEX_DOC>\n" +
                "<DOC_REQUISITIONS DOC_DATE=\"2023-08-08\" DOC_TIME=\"17:27:01\" SENDER_ID=\"550\" RECEIVER_ID=\"263\" DOC_NO=\"123456\" REMARKS=\"Порядковый номер отбора 297\"/>\n" +
                "<KDX10 VER=\"1.0\">\n" +
                "<KDX10_REC " +
                "AUCTION_ID=\"" + id + "\" " +
                "SECURITYID=\"DK1000K006R\" " +
                "AUCTION_DATE=\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "FIRST_DATE=\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "SECOND_DATE=\"" + LocalDateTime.now().plusDays(6).format(DateTimeFormatter.ofPattern(DateFormat.XML_DATE_FORMAT.getValue())) + "\" " +
                "TERM=\"6\" CURR=\"RUB\" " +
                "MIN_RATE=\"0.1299\" " +
                "MAX_VALUE=\"100000000\" " +
                "FORM=\"O\" " +
                "CONDITION=\"P\" " +
                "MAX_NUMBER=\"35\" " +
                "MIN_VALUE=\"3000\" " +
                "START_TIME_1=\"" + LocalDateTime.now().plusMinutes(3).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_1=\"" + LocalDateTime.now().plusMinutes(8).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "MIDDLE_TIME_1=\"" + LocalDateTime.now().plusMinutes(6).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_2=\"" + LocalDateTime.now().plusMinutes(9).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "END_TIME_3=\"" + LocalDateTime.now().plusMinutes(14).format(DateTimeFormatter.ofPattern(DateFormat.XML_TIME_FORMAT.getValue())) + "\" " +
                "RATE_TYPE=\"FLOATING\"/>\n" +
                "</KDX10>\n" +
                "</SPCEX_DOC>";
        File file = File.createTempFile("test268_", ".xml");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(s);
        fileWriter.close();
        return file;
    }

    @Step("Загрузить файл")
    public AuctionInitSteps uploadFileParams(File file) {
        OpenedAucOrTradeParamsPage.getInstance().uploadFile(file);
        return this;
    }

    @Step("Поставить ссылку на резутьтаты аукциона")
    public AuctionInitSteps setResUrl(String value) {
        WaitingUtils.sleep(1);
        OpenedAurOrTradeResultsPage.getInstance().fillResUrl(value);
        return this;
    }

    @Step("Сохранить")
    public AuctionInitSteps saveRes() {
        OpenedAurOrTradeResultsPage.getInstance().save();
        return this;
    }

    @Step("Опубликовать")
    public AuctionInitSteps publish() {
        OpenedAurOrTradeResultsPage.getInstance().publish();
        return this;
    }

    @Step("Проверить, что на sftp генерировались файлы по маске")
    public AuctionInitSteps checkSftp(List<String> list, Pattern pattern) {
        Predicate<String> predicate = i -> {
            Matcher matcher = pattern.matcher(i);
            return matcher.matches();
        };
        Assertions.assertThat(list.stream().anyMatch(predicate))
                .as("Проверить, что сгенерировался файл по маске + " + predicate + " : " + list)
                .isTrue();
        return this;
    }

    public AuctionInitSteps setReturnDate(String value) {
        OpenedAucOrTradeParamsPage.getInstance().setReturnDate(value);
        return this;
    }

    @Step("Нажать кнопку 'Получить статус из НРД'")
    public AuctionInitSteps getNRDStatus() {
        OpenedAucOrTradeNRDPage.getInstance().getStatus();
        return this;
    }

    @Step("Проверить, что появилась кнопка 'Скачать ответ НРД'")
    public AuctionInitSteps checkNRDDownloadExists() {
        OpenedAucOrTradeNRDPage.getInstance().checkDownloadExists();
        return this;
    }

    @Step("Выполнить перезагрузку модуля брокерских котировок")
    public AuctionInitSteps refreshBroker() {
        new DbHelper(Credentials.getInstance().dbArkUrl(), Credentials.getInstance().dbArkLogin(), Credentials.getInstance().dbArkPassword())
                .exec("EXEC xp_cmdshell 'powershell -c c:\\Script\\BQRestart.ps1',no_output;");
        return this;
    }

    @Step("Зайти в Quik и выставить заявки на покупку")
    public AuctionInitSteps loginQuikCreateApl(FilePath quik, String bankName, String stockName, List<List<String>> applications) {
        new AuctionSteps().loginQuik(quik);
        new CurrentTradesPage().openCurrentTrades(bankName)
                .checkTradingStatus(stockName)
                .getRowByStock(stockName);
        for (List<String> apl : applications) {
            RequestPage.getInstance().makeRequest(apl.get(0), apl.get(1));
        }
        return this;
    }

    @Step("Зайти в Quik и выставить заявки на покупку")
    public AuctionInitSteps loginQuikCreateApl(FilePath quik, String stockCode, List<List<String>> applications) {
        new AuctionSteps().loginQuik(quik);
        new CurrentTradesPage()
                .openCurrentTrades(stockCode)
                .checkTradingStatus(stockCode);
        new CurrentTradesPage().getRowByStock(stockCode);
        RequestPage requestPage = RequestPage.getInstance();
        for (List<String> apl : applications) {
            requestPage.makeRequest(apl.get(0), apl.get(1));
        }
        return this;
    }

    public AuctionInitSteps closeDesktop() {
        new AuctionSteps().closeDesktop();
        return this;
    }

    @Step("Изменить процентную ставку по заявке номер '{requestNumber}' на 'percent'")
    public AuctionInitSteps raiseBidWithOpenWindow(String requestNumber, String percent) {
        AuctionSteps auctionSteps = new AuctionSteps().createQuikWindow("Заявки");
        new RequestPage()
                .saveToFile()
                .getRowByRequestNumber(requestNumber)
                .setPercentBox(percent);
        return this;
    }

    @Step("Изменить процентную ставку по заявке номер '{requestNumber}' на 'percent'")
    public AuctionInitSteps raiseBid(String percent) {
        new Actions(DesktopConfiguration.driver).keyDown(CONTROL).keyDown(END).keyUp(END).keyUp(CONTROL).perform();
        new RequestPage().setPercentBox(percent);
        return this;
    }

    @Step("Проверить процентную ставку по последней заявке")
    public AuctionInitSteps checkBid(double percent) {
        new RequestPage()
                .deleteSavedData()
                .saveToFile()
                .checkLastReqRate(percent);

        return this;
    }

    @Step("Зайти в Quik и выставить заявки на покупку с ошибкой")
    public AuctionInitSteps loginQuikCreateAplError(FilePath quik, String bankName, String stockName, List<List<String>> applications, String error) {
        Credentials.setEnv("test");
        new AuctionSteps().loginQuik(quik);
        new CurrentTradesPage().openCurrentTrades(bankName)
                .checkTradingStatus(stockName);
        new CurrentTradesPage().getRowByStock(stockName);
        for (List<String> apl : applications) {
            new RequestPage().makeRequestError(apl.get(0), apl.get(1), error);
        }
        return new AuctionInitSteps();
    }

    @Step("Выставить условие проведенияя аукциона: Минимальная сумма в заявке УТ - {value}")
    public AuctionInitSteps setMinSumCon(String value) {
        DepositAuctionNTradesPage.getInstance().setMinSumCon(value);
        return this;
    }

    @Step("Выставить условие проведенияя аукциона: Максимальное количество заявок от 1 участника - {value}")
    public AuctionInitSteps setMaxAplCon(String value) {
        DepositAuctionNTradesPage.getInstance().setMaxAplCon(value);
        return this;
    }

    @Step("Выставить диапозон времени режима на повышение")
    public AuctionInitSteps setRateIncreaseTime(String from, String to) {
        OpenedAucOrTradeParamsPage.getInstance().setRateIncreaseTime(from, to);
        return this;
    }

    @Step("Изменить диапозон времери режима на повышение")
    public AuctionInitSteps changeTime(String aplFrom, String aplTo, String incFrom, String incTo, String endTo) {
        OpenedAucOrTradeParamsPage.getInstance()
                .changeTime(aplFrom, aplTo)
                .changeRateIncreaseTime(incFrom, incTo)
                .changeTimeBetTo(endTo);
        return this;
    }

    @Step("Редактировать объявление")
    public AuctionInitSteps edit() {
        OpenedAucOrTradeParamsPage.getInstance().edit();
        return this;
    }

    @Step("Проверить, что все поданные заявки верно отображаются")
    public AuctionInitSteps checkApl(List<List<String>> apl, String bankName) {
        List<WebElement> list = OpenedAucOrTradeApplicationsPage.getInstance().getApl();
        $x("//td[2]").should(Condition.visible);
        List<ApplicationDto> aplQuik = new ArrayList<>();
        List<ApplicationDto> aplWeb = new ArrayList<>();
        WaitingUtils.sleep(1);
        for (List<String> l : apl) {
            aplQuik.add(new ApplicationDto(bankName, l.get(0), l.get(1)));
        }
        for (WebElement element : list) {
            aplWeb.add(new ApplicationDto(
                    element.findElement(By.xpath("./td[2]")).getText(),
                    element.findElement(By.xpath("./td[5]/div")).getText(),
                    element.findElement(By.xpath("./td[6]/div")).getText()));
        }
        Assertions.assertThat(aplQuik)
                .as("Проверить, что все поданные заявки верно отображаются")
                .hasSameElementsAs(aplWeb);
        return new AuctionInitSteps();
    }

    @Step("Проверить, что все поданные заявки верно отображаются")
    public AuctionInitSteps checkRealApl(List<List<String>> apl, String bankName) {
        $x("//td[2]").shouldBe(Condition.visible);
        $x("//td[6]").shouldBe(Condition.visible);
        $x("//td[7]").shouldBe(Condition.visible);
        List<WebElement> list = OpenedAucOrTradeApplicationsPage.getInstance().getApl();
        List<ApplicationDto> aplQuik = new ArrayList<>();
        List<ApplicationDto> aplWeb = new ArrayList<>();
        WaitingUtils.sleep(1);
        for (List<String> l : apl) {
            aplQuik.add(new ApplicationDto(bankName, l.get(0), l.get(1)));
        }
        for (WebElement element : list) {
            aplWeb.add(new ApplicationDto(
                    element.findElement(By.xpath("./td[2]")).getText(),
                    element.findElement(By.xpath("./td[6]/div")).getText(),
                    element.findElement(By.xpath("./td[7]/div")).getText()));
        }
        Assertions.assertThat(aplQuik)
                .as("Проверить, что все поданные заявки верно отображаются")
                .hasSameElementsAs(aplWeb);
        return this;
    }

    @Step("Подождать до {time}")
    public AuctionInitSteps waitUntil(LocalDateTime time) {
        WaitingUtils.waitUntil(time);
        return this;
    }

    @Step("Выставить вид ставки дедепозита")
    public AuctionInitSteps setTypeOfDepositRate(String value) {
        OpenedAucOrTradeParamsPage.getInstance().setTypeOfDepositRate(value);
        return this;
    }

    @Step("Выставить способ удовлетворения заявок")
    public AuctionInitSteps setSatisfyingApplications(String value) {
        OpenedAucOrTradeParamsPage.getInstance().setSatisfyingApplications(value);
        return this;
    }

    @Step("Выставить заявки на покупку с ошибкой")
    public AuctionInitSteps createAplError(List<List<String>> applications, String error) {
        Credentials.setEnv("test");
        for (List<String> apl : applications) {
            new RequestPage().makeRequestError(apl.get(0), apl.get(1), error);
        }
        return this;
    }

    @Step("Выставить заявки на покупку")
    public AuctionInitSteps createApl(List<List<String>> applications) {
        Credentials.setEnv("test");
        for (List<String> apl : applications) {
            new RequestPage().makeRequest(apl.get(0), apl.get(1));
        }
        return this;
    }

    public List<List<List<String>>> getApllSPB_T296() {
        List<List<String>> listLowPer = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        list1.add("9");
        list1.add("1000000");
        List<List<String>> listLowSum = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("10");
        list2.add("300");
        List<List<String>> listOK = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        list3.add("10");
        list3.add("400");
        List<String> list4 = new ArrayList<>();
        list4.add("10");
        list4.add("400");
        List<String> list5 = new ArrayList<>();
        list5.add("10");
        list5.add("400");
        List<String> list6 = new ArrayList<>();
        list6.add("10");
        list6.add("400");
        List<List<String>> listTooMUchAppl = new ArrayList<>();
        List<String> list7 = new ArrayList<>();
        list7.add("11");
        list7.add("500");
        listLowPer.add(list1);
        listLowSum.add(list2);
        listOK.add(list3);
        listOK.add(list4);
        listOK.add(list5);
        listOK.add(list6);
        listTooMUchAppl.add(list7);
        List<List<List<String>>> data = new ArrayList<>();
        data.add(listLowPer);
        data.add(listLowSum);
        data.add(listOK);
        data.add(listTooMUchAppl);
        return data;
    }

    @Step("Дождаться завершения по документу {0}")
    public AuctionInitSteps waitMkrProcessFinish(String fileName) {
        openSiteGoToDocRegistry();
        for (int i = 0; i < 30; i++) {
            new FilterSteps().setFilter(NAME_DOC, List.of(fileName))
                    .setFilter(STATUS, List.of(Status.COMPLETE.getValue()));
            List<String> docs = FilterInitiators.getInstance().getColumnStrings(AuctionInitStandardColumn.NAME_DOC);
            if (!docs.contains("Нет данных")) {
                return this;
            }
            Selenide.sleep(10*1000);
        }
        throw new RuntimeException("Не появился документ");
    }

    @Step("Проверить, что документ {0} есть в таблице")
    public AuctionInitSteps checkMkrDocExist(String fileName, SoftAssertions softly) {
        new FilterSteps().checkFilteredColumn(AuctionInitStandardColumn.NAME_DOC, fileName, softly);
        return this;
    }

    @Step("Выполнить удаление экспортированных инструментов в ARQA")
    public AuctionInitSteps deleteSecCodes(String fileName) {
        List<String> secCodes = getMkrSecCodes(fileName);
        for (String str : secCodes) {
            new MkrDbHelper().deleteInstrument(str);
        }
        return this;
    }

    private List<String> getMkrSecCodes(String fileName) {
        SftpUtils.downloadFile(fileName, INITIATORS_MKR_SECURITIES.getValue(), TEMP_FILES.getValue());
        Document doc = XmlUtils.parseXml(TEMP_FILES.getValue() + fileName);
        List secs = doc.selectNodes("//SECURITY/@ID");
        List<String> instruments = new ArrayList<>();
        for (Object el : secs) {
            instruments.add(((DefaultAttribute) el).getText());
        }
        return instruments;
    }

    @Step("Проверить в БД ARQA наличие загруженных инструментов")
    public AuctionInitSteps checkSecCodesInDb(String fileName, SoftAssertions softly) {
        List<String> secCodes = getMkrSecCodes(fileName);
        softly.assertThat(new MkrDbHelper().getDistinctSecCodes(secCodes))
                .as("Проверить загруженные инструменты")
                .hasSameElementsAs(secCodes);
        return this;
    }

    public int getAucCount(String stockName) {
        int count = InitRoutineOperationsPage.getInstance()
                .openSection(InitiatorsPage.UiInitiatorsSections.ROUTINE_OPERATIONS, InitRoutineOperationsPage.class)
                .filterProcess(stockName)
                .countFinishedProcess(stockName);
        new InitiatorsPage().openSection(DEPOSIT_AUCTION_N_TRADES, DepositAuctionNTradesPage.class);
        return count;
    }

    @SneakyThrows
    @Step("Скачать и проверить depositregistry_response")
    public AuctionInitSteps downloadAndCheckSentDealsNRD(Map<String, String> data) {
        UiButton button = OpenedAucOrTradeNRDPage.getInstance().downloadNrdSentRegistry;
        File file = button.download();
        String str = Files.readString(Path.of(file.getAbsolutePath()), Charset.forName("windows-1251"));
        for (Map.Entry<String, String> entry : data.entrySet()) {
            str.contains(String.format("\"%s\":\"%s\"", entry.getKey(), entry.getValue()));
        }
        return this;
    }

    @Step("Проверить наличие кнопки 'Скачать реестр отправленных сделок в НРД'")
    public AuctionInitSteps checkDownloadRegistrySentNrdButtonExist() {
        UiButton button = OpenedAucOrTradeNRDPage.getInstance().downloadNrdSentRegistry;
        WaitingUtils.waitUntil(20, 1, 1, "Проверить наличие кнопки 'Скачать реестр отправленных сделок в НРД'",
                () -> button.getElement().isEnabled());
        return this;
    }

    @Step("Проверить наличие строки в таблице на вкладке НРД")
    public AuctionInitSteps checkTableNrdRow(String rate, String result) {
        ElementsCollection rows = $$x("//tbody//tr");

        WaitingUtils.waitUntil(30, 1, 1, String.format("Проверить, что есть строка со ставкой %s и результатом %s", rate, result), () -> {
            boolean isRowExist = false;
            for (SelenideElement row : rows) {
                isRowExist = row.$x(".//td[5]//div").should(enabled, Duration.ofSeconds(5)).getText().contains(rate)
                        && row.$x(".//td[8]").should(enabled, Duration.ofSeconds(5)).getText().contains(result);
                if (isRowExist)
                    return true;
            }
            return false;
        });
        return this;
    }

    @SneakyThrows
    @Step("Скачать json ответ НРД и проверить поле 'result'")
    public AuctionInitSteps downloadAndCheckNrdAnswer(String result) {
        File file = $x("//button[contains(.,'Скачать ответ НРД')]").download();
        String str = Files.readString(Path.of(file.getAbsolutePath()), Charset.forName("windows-1251"));
        Assertions.assertThat(str)
                .contains(String.format("\"result\":\"%s\"", result));
        return this;
    }

    @Step("Обновить список сделок")
    public AuctionInitSteps updateTrades(){
        OpenedAucOrTradeTradesPage.getInstance().update();
        return this;
    }

    public AuctionInitSteps checkNrd(String biCode) {
        LocalDateTime from = LocalDateTime.now().minusDays(14).withHour(0).withMinute(0).withSecond(1);
        LocalDateTime to = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(1);
        openSiteGoToInitAuc();
        openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF);
        FilterSteps filterSteps = new FilterSteps()
                .setFilter(InitiatorsPage.FilterColumns.EXCHANGE_INSTRUMENT, List.of(biCode))
                .setFilter(InitiatorsPage.FilterColumns.DATE, List.of(
                        from.format(ofPattern(XML_DATE_FORMAT.getValue())),
                        to.format(ofPattern(XML_DATE_FORMAT.getValue())))
                ).setFilter(InitiatorsPage.FilterColumns.STATUS, List.of(Status.COMPLETE.getValue()));
        List<String> docs = FilterInitiators.getInstance().getColumnStrings(OverCountRepoColumn.NAME_DOC);
        if (!docs.isEmpty() && docs.get(0).contains("Нет данных"))
            return this;
        Selenide.refresh();

        for (String doc : docs) {
            SpvbUtils.step("Проверить статус на вкладке НРД в документе " + doc);
            filterSteps.setFilter(NAME_DOC, List.of(doc));
            openTradeOrAuc(doc);
            openSection(OpenAucOrTradeHeaderPage.Section.NRD, OpenedAucOrTradeNRDPage.getInstance());
            $x("//*[contains(@class,'MuiBackdrop-root')]").should(enabled)
                    .should(disappear);
            String docStatus = OverCountRepoAppPage.getInstance().docStatus().getText();
            if (docStatus.contains(DocStatus.IN_WORK.getValue())) {
                OpenedAucOrTradeNRDPage.getInstance().getStatus();
            }
            OverCountRepoAppPage.getInstance().checkDocumentStatus(DocStatus.FINISHED);
            openSiteGoToInitAuc();
            openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF);
        }
        return this;
    }

    @Step("Проверить, что сегдняшний аукцион отклонился")
    public AuctionInitSteps checkAucDeclined(String biCode, DepositAuctionNTradesPage.UiAuctionsNTradeSection section, SoftAssertions softly) {
        openSiteGoToInitAuc();
        openSection(section);
        List<Throwable> list = softly.errorsCollected();
        new FilterSteps()
                .setFilter(EXCHANGE_INSTRUMENT, List.of(biCode))
                .setFilter(DATE, List.of(
                        LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue())),
                        LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue()))
                )).checkFilteredColumnContains(AuctionInitStandardColumn.STATUS, List.of(DECLINED.getStatus()), softly);
        if (list.size() < softly.errorsCollected().size()) {
            Allure.getLifecycle().updateStep(testResult -> testResult.setStatus(io.qameta.allure.model.Status.FAILED));
            Allure.getLifecycle().stopStep();
        }
        return this;
    }

    @Step("Проверить, что сегодня отсутствуют аукционы в статусе 'Новый'")
    public AuctionInitSteps checkNewAucNotExist(DepositAuctionNTradesPage.UiAuctionsNTradeSection section, SoftAssertions softly) {
        openSiteGoToInitAuc();
        openSection(section);
        List<Throwable> list = softly.errorsCollected();
        new FilterSteps()
                .setFilter(STATUS, List.of(OpenAucOrTradeHeaderPage.GeneralStatus.NEW.getStatus()))
                .setFilter(DATE, List.of(
                        LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue())),
                        LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue()))
                )).checkNoData(softly);
        if (list.size() < softly.errorsCollected().size()) {
            Allure.getLifecycle().updateStep(testResult -> testResult.setStatus(io.qameta.allure.model.Status.FAILED));
            Allure.getLifecycle().stopStep();
        }
        return this;
    }
}
