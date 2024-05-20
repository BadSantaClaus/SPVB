package ru.spvb.steps.auction;

import com.codeborne.selenide.Selenide;
import config.DesktopConfiguration;
import constants.*;
import db.DbHelper;
import elements.columns.BankRussiaProcessColumn;
import elements.columns.BankRussiaStepsColumn;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.assertj.core.api.SoftAssertions;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import page.quik.*;
import page.web.*;
import utils.*;

import java.io.*;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static constants.CmdCommand.UNDERWRITE_FILE;
import static constants.CmdCommand.UNDERWRITE_ZPFX;
import static constants.DateFormat.XML_DATE_FORMAT;
import static constants.DateFormat.XML_TIME_FORMAT;
import static constants.DecimalFormats.BIG_DOUBLE;
import static constants.Extension.*;
import static constants.FileName.*;
import static constants.FilePath.*;
import static constants.ProcessName.*;
import static constants.Status.COMPLETE;
import static constants.XmlAttribute.LIMIT;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.openqa.selenium.Keys.DOWN;
import static utils.SftpUtils.downloadFile;

@Slf4j
@SuppressWarnings({"UnusedReturnValue", "unchecked", "unused"})
public class AuctionSteps {
    private final DynamicTransactionPage dynamicTransactionPage = new DynamicTransactionPage();
    private final AuthorizationQuikPage authorizationQuikPage = new AuthorizationQuikPage();
    private File cbrTri;
    private File cbrTro;

    @Step("Создать файл PFX41")
    public AuctionSteps createPfx41(String pfxName) {
        PfxUtils.parseXmlWithDateAndAucId(PFX41, pfxName);
        return this;
    }

    @Step("Поместить файл на сфтп в папку br/in")
    public AuctionSteps copyFile(String filePath) {
        SftpUtils.uploadFile(Path.of(filePath), "br/in/");
        return this;
    }

    @Step("Подписать файл электронной подписью")
    public AuctionSteps signFile(String fileName) {
        SftpUtils sftpUtils = new SftpUtils();
        String cmdCommand = String.format(UNDERWRITE_FILE.getValue(), fileName);
        sftpUtils.executeCommandWithPassword(cmdCommand, Credentials.getInstance().sshUnderWritePassword());
        log.info("Подписать файл электронной подписью");
        return this;
    }

    @Step("Подписать zPfx файл электронной подписью")
    public AuctionSteps signFileZpfx(String fileName) {
        SftpUtils sftpUtils = new SftpUtils();
        String cmdCommand = String.format(UNDERWRITE_ZPFX.getValue(), fileName);
        sftpUtils.executeCommand(cmdCommand);
        log.info("Подписать zPfx файл электронной подписью");
        return this;
    }

    public AuctionSteps signFileBrIn(String fileName) {
        signFile(BR_IN.getValue() + fileName);
        return this;
    }

    @Step("Изменить название файла [{oldFileName}] на [{newFileName}]")
    public AuctionSteps renameFile(String directory, String oldFileName, String newFileName) {
        SftpUtils sftpUtils = new SftpUtils();
        String command = "mv %s%s %s%s";
        sftpUtils.executeCommand(String.format(command, directory, oldFileName, directory, newFileName));
        log.info("Изменить расширение файла");
        return this;
    }

    @Step("Выставить заявки на покупку в Quik")
    public AuctionSteps sendRequestByQuik(String stockName, Map<String, String> priceQuantity, String clientCode) {
        new CurrentTradesPage()
                .openAuctionCurrentTrades(stockName)
                .checkTradingStatus(stockName);
        RequestPage requestPage = new RequestPage();
        priceQuantity.forEach((key, value) -> requestPage.makeRequest(key, value, clientCode));
        return this;
    }

    @Step("Выполнить перезагрузку модуля брокерских котировок")
    public AuctionSteps refreshBroker() {
        new DbHelper(Credentials.getInstance().dbArkUrl(), Credentials.getInstance().dbArkLogin(), Credentials.getInstance().dbArkPassword())
                .exec("EXEC xp_cmdshell 'powershell -c c:\\Script\\BQRestart.ps1',no_output;");
        log.info("Выполнить перезагрузку модуля брокерских котировок");
        return this;
    }

    @Step("Поместить {count} файлов {name} в br/in")
    public AuctionSteps sendZFiles(FileName name, int count) {
        for (int i = 0; i < count; i++) {
            File file = PfxUtils.createZFile(name);
            copyFile(file.getAbsolutePath());
            signFileZpfx(BR_IN.getValue() + file.getName());
            renameFile(BR_IN.getValue(), file.getName() + SIG.getValue(), file.getName() + P7S.getValue());
            SftpUtils.deleteFile(BR_IN.getValue() + file.getName());
            log.info(String.format("Отправить на sftp и подписать файл %s", file.getName()));
        }
        return this;
    }

    @Step("Импортировать транзакции из файлов CBR.tri, CBR.tro в квик")
    public AuctionSteps processDynamicTransaction() {
        loginQuik(QUIK_CB);
        dynamicTransactionPage.openImportTransactionFromFile();
        dynamicTransactionPage.processData(cbrTri.getAbsolutePath(), cbrTro.getAbsolutePath());
        return this;
    }

    @Step("Проверить, что число выполненных транзакций равно {expectedNumber}")
    public AuctionSteps checkNumberOfTransactionsComplete(int expectedNumber) {
        new DynamicTransactionPage().checkNumberOfTransactionsComplete(expectedNumber);
        return this;
    }

    @Step("Проверить, что файл pfx41 обработан")
    public AuctionSteps checkPfx41Status(String fileName) {
        AuthorizationWebPage.getInstance().loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .openProcess(PFX41_GET_FROM_BR.getValue())
                .scrollProcessTableBottom()
                .checkPfx41Status(fileName,
                        () -> new BankRussiaPage()
                                .openProcess(PFX41_GET_FROM_BR.getValue())
                                .scrollProcessTableBottom());
        return this;
    }

    @Step("Проверить, что все аукционы из файла '{pfx41Name}' отображаются на странице")
    public AuctionSteps checkAuctionsExist(String pfx41Name) {
        String assertMessage = "Проверить, что все аукционы из файла pfx41 отображаются на странице";
        BankRussiaPage.getInstance()
                .filterProcesses(AUCTION_TEXT.getValue())
                .checkProcessIsActive(PFX41, AUCTION_TEXT.getValue(), assertMessage,
                        () -> BankRussiaPage.getInstance()
                                .filterProcesses(AUCTION_TEXT.getValue()));
        return this;
    }

    @Step("Подготовить файл pfx39")
    public AuctionSteps createPfx39_t113(String pfx38Name, String pfx39Name) {
        PfxUtils.createPfx39_t113(pfx38Name, pfx39Name);
        return this;
    }

    @Step("Подготовить файл pfx39")
    public AuctionSteps createPfx39_t97(String pfx38Name, String pfx39Name) {
        PfxUtils.createPfx39_t97(pfx38Name, pfx39Name);
        return this;
    }

    @Step("Подготовить файл pfx39")
    public AuctionSteps createPfx39_t87(String pfx38Name, String pfx39Name) {
        PfxUtils.createPfx39_t87(pfx38Name, pfx39Name);
        return this;
    }

    @Step("Проверить статус выполнения процесса pfx38")
    public AuctionSteps checkPfx38Status(String stockCode) {
        AuthorizationWebPage.getInstance().loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .filterProcesses(PFX38_SEND_TO_BR.getValue())
                .expandProcessName(String.format("Аукцион (торговый код бумаги: %s)", stockCode), PFX38_SEND_TO_BR.getValue())
                .checkAllStepsFinishedStatus("PFX38. Отправка в БР информации о заявках, принятых на аукцион",
                        () -> new BankRussiaPage()
                                .filterProcesses(PFX38_SEND_TO_BR.getValue())
                                .expandProcessName(String.format("Аукцион (торговый код бумаги: %s)", stockCode), PFX38_SEND_TO_BR.getValue()))
                .clearProcessFilter();
        return this;
    }

    @Step("Проверить статус выполнения процесса Pfx39")
    public AuctionSteps checkPfx39Status(String pfx39Name, String stockCode) {
        String processName = String.format("PFX39. Получение из БР реестра заявок, подлежащих удовлетворению (%s)", pfx39Name);
        BankRussiaPage.getInstance()
                .filterProcesses(processName)
                .expandProcessName(PFX39_MONITORING.getValue(), processName)
                .checkAllStepsFinishedStatus(processName,
                        () -> new BankRussiaPage()
                                .filterProcesses(processName)
                                .expandProcessName(PFX39_MONITORING.getValue(), processName))
                .filterProcesses(PFX39_SEND_TO_BR.getValue())
                .expandProcessName(String.format("Аукцион (торговый код бумаги: %s)", stockCode), PFX39_SEND_TO_BR.getValue())
                .checkAllStepsFinishedStatus(PFX39_SEND_TO_BR.getValue(), Collections.singletonList(IMPORT_FROM_QUIK.getValue()),
                        () -> new BankRussiaPage()
                                .filterProcesses(PFX39_SEND_TO_BR.getValue())
                                .expandProcessName(String.format("Аукцион (торговый код бумаги: %s)", stockCode), PFX39_SEND_TO_BR.getValue()));
        return this;
    }

    @Step("Скачать файл CBR.tri экспорта заявок БР в QUIK (аукцион)")
    public AuctionSteps downloadAuctionCbrFile() {
        cbrTri = BankRussiaPage.getInstance().downloadAuctionCbrFile();
        return this;
    }

    @SneakyThrows
    @Step("Подготовить файл CBR.tri (оставить заявки только по инструменту {stockCode})")
    public AuctionSteps prepareAuctionCbrTriFile(String stockCode) {
        String cbr = SpvbUtils.readFromFile(cbrTri.getAbsolutePath());
        List<String> results = Arrays.asList(cbr.split("TRANS_ID="));
        String result = results.stream()
                .filter(s -> s.contains(stockCode))
                .map(s -> "TRANS_ID=" + s)
                .collect(Collectors.joining());
        result = result.trim();
        Allure.addAttachment("CBR.tri", result);
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("target/temp/CBR.tri"), "Windows-1251"))) {
            out.println(result);
        }
        cbrTri = new File("target/temp/CBR.tri");
        return this;
    }

    @SneakyThrows
    @Step("Подготовить файл CBR.tri (оставить заявки только по инструменту {stockCode})")
    public AuctionSteps prepareExtraReplacementCbrTriFile(String stockCode) {
        String cbr = SpvbUtils.readFromFile(cbrTri.getAbsolutePath());
        List<String> results = Arrays.asList(cbr.split("TRANS_ID="));
        String result = results.stream()
                .filter(s -> s.contains(stockCode) && s.contains("Контрагент"))
                .map(s -> "TRANS_ID=" + s)
                .collect(Collectors.joining());
        result = result.trim();
        Allure.addAttachment("CBR.tri", result);
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream("target/temp/CBR.tri"), "Windows-1251"))) {
            out.println(result);
        }
        cbrTri = new File("target/temp/CBR.tri");
        return this;
    }

    @Step("Скачать файл CBR.tri экспорта встречных заявок РЕПО")
    public AuctionSteps downloadRepoCbrFile() {
        cbrTri = BankRussiaPage.getInstance().downloadRepoCbrFile();
        return this;
    }

    @Step("Скачать файл CBR.tri экспорта заявок БР в QUIK (доразмещение)")
    public AuctionSteps downloadExtraPlacementCbrFile() {
        cbrTri = BankRussiaPage.getInstance().downloadExtraPlacementCbr();
        return this;
    }

    @SneakyThrows
    @Step("Подготовить файл CBR.tro")
    public AuctionSteps prepareCbrTroFile() {
        cbrTro = new File(TEMP_FILES.getValue() + "CBR.tro");
        if (!cbrTro.createNewFile()) {
            try (FileWriter fileWriter = new FileWriter(cbrTro, false)) {
                fileWriter.write("");
            }
        }
        return this;
    }

    @Step("Проверить, что аукцион по цб {stockCode} завершен")
    public AuctionSteps checkSuccessAuctionStatus(String stockCode) {
        AuthorizationWebPage.getInstance()
                .loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .filterProcesses(String.format("Аукцион (торговый код бумаги: %s)", stockCode));
        BankRussiaPage.getInstance().waitProcessComplete(String.format("Аукцион (торговый код бумаги: %s)", stockCode));
        return this;
    }

    @Step("Проверить, что аукцион по цб {stockCode} завершен с ошибкой")
    public AuctionSteps checkFailAuctionStatus(String stockCode) {
        AuthorizationWebPage.getInstance()
                .loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .filterProcesses(String.format("Аукцион (торговый код бумаги: %s)", stockCode));
        BankRussiaPage.getInstance()
                .waitProcessFail(String.format("Аукцион (торговый код бумаги: %s)", stockCode))
                .closeWebDriver();
        return this;
    }

    public AuctionSteps closeWebDriver() {
        BankRussiaPage.getInstance().closeWebDriver();
        return this;
    }

    public AuctionSteps closeDesktop() {
        DesktopConfiguration.close();
        return this;
    }

    @Step("Подготовить файл pfx42")
    public AuctionSteps createPfx42(FileName pfx, String pfx42Name) {
        PfxUtils.parseXmlWithDateAndAucId(pfx, pfx42Name);
        return this;
    }

    @Step("Проверить, что файл pfx42 обработан")
    public AuctionSteps checkPfx42Status(String processName, String pfx42Name) {
        AuthorizationWebPage.getInstance()
                .loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class);
        BankRussiaPage.getInstance()
                .checkPfx42Status(processName, pfx42Name)
                .clearProcessFilter();
        return this;
    }

    @Step("Проверить, что время начала процесса в файле соответствует планируемому времени старта на странице")
    public AuctionSteps checkProcessStartTime(String pfx42Name, String stockCode) {
        String processName = String.format("Доразмещение (торговый код бумаги: %s)", stockCode);
        BankRussiaPage.getInstance()
                .checkProcessStartTime(processName, pfx42Name, stockCode)
                .expandProcessName(processName)
                .checkProcessStartTime(EXPORT_QUIK.getValue(), pfx42Name, stockCode);
        return this;
    }

    @Step("Проверить, что все доразмещения из файла {fileName} отображаются на странице")
    public AuctionSteps checkExtraReplacementsExist(FileName pfx, String containsText) {
        String assertMessage = "Проверить, что все доразмещения из файла pfx42 отображаются на странице";
        BankRussiaPage.getInstance()
                .filterProcesses(EXPORT_QUIK.getValue())
                .checkProcessIsActive(pfx, containsText, assertMessage,
                        () -> BankRussiaPage.getInstance()
                                .filterProcesses(EXPORT_QUIK.getValue()));
        return this;
    }

    @Step("Проверить, что процесс \"{pfx43ProcessName}\" успешно завершен")
    public AuctionSteps checkPfx43RequestStatus(String containsText, String pfx43ProcessName) {
        AuthorizationWebPage.getInstance()
                .loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class);
        BankRussiaPage.getInstance()
                .filterProcesses(pfx43ProcessName)
                .expandProcessName(containsText, pfx43ProcessName)
                .checkAllStepsFinishedStatus(pfx43ProcessName, () ->
                        new BankRussiaPage()
                                .filterProcesses(pfx43ProcessName)
                                .expandProcessName(containsText, pfx43ProcessName));
        return this;
    }

    @Step("Подготовить файл pfx43")
    public AuctionSteps createPfx43(String pfx43RemoteName, String pfx43IncreaseIndexName) {
        PfxUtils.createPfx43(pfx43RemoteName, pfx43IncreaseIndexName);
        return this;
    }

    @Step("Проверить, что процесс \"{pfxMonitoring}\" успешно завершен")
    public AuctionSteps checkPfx43MonitoringStatus(String pfxMonitoring, String pfx43Name) {
        String processName = String.format("PFX43. Получение из БР подписанных заявок БР на продажу (%s)", pfx43Name);
        BankRussiaPage.getInstance()
                .filterProcesses(processName)
                .expandProcessName(pfxMonitoring, processName)
                .checkAllStepsFinishedStatus(processName,
                        () -> new BankRussiaPage()
                                .filterProcesses(processName)
                                .expandProcessName(pfxMonitoring, PFX43_GET_FROM_BR.getValue()))
                .clearProcessFilter();
        return this;
    }

    @Step("Проверить, что процесс \"{pfx43ExportQuikName}\" успешно завершен")
    public AuctionSteps checkPfx43ExportQuikStatus(String pfx43ExportQuikName, String containsText, String pfx43ProcessName) {
        BankRussiaPage.getInstance()
                .filterProcesses(pfx43ExportQuikName)
                .expandProcessName(containsText, pfx43ProcessName)
                .checkAllStepsFinishedStatus(pfx43ExportQuikName,
                        () -> new BankRussiaPage()
                                .filterProcesses(pfx43ExportQuikName)
                                .expandProcessName(containsText, pfx43ProcessName));
        return this;
    }

    @Step("Выставить заявки на покупку по внебиржевым сделкам в Quik ")
    public AuctionSteps sendOutStockExchangeRequests(String stockCode, FileName pfx, String clientCode, int expectedNumberOfTransactionsComplete) {
        loginQuik(QUIK_VTB);
        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(pfx, "END_TIME", stockCode));
        new OutStockExchangePage()
                .openOutStockExchange()
                .selectRowByStockCode(stockCode);
        RequestPage requestPage = new RequestPage().makeRequest(clientCode);
        WaitingUtils.sleep(2);
        DesktopUtils.takeScreenshot();

        expectedNumberOfTransactionsComplete -= 1;
        for (int i = 0; i < expectedNumberOfTransactionsComplete; i++) {
            DesktopUtils.pressKey(DOWN);
            requestPage.makeRequest(clientCode);
            WaitingUtils.sleep(2);
            DesktopUtils.takeScreenshot();
        }
        return this;
    }

    @Step("Проверить, что доразмещение по инструменту с кодом '{stockCode}' завершено успешно")
    public AuctionSteps checkExtraReplacementStatus(String stockCode) {
        AuthorizationWebPage.getInstance()
                .loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .filterProcesses(EXTRA_REPLACEMENT_TEXT.getValue() + stockCode);
        BankRussiaPage.getInstance()
                .waitProcessComplete(EXTRA_REPLACEMENT_TEXT.getValue() + stockCode)
                .closeWebDriver();
        return this;
    }

    @Step("Открыть процесс с планируемым временем старта")
    public AuctionSteps openProcessWithPlannedStart(ProcessName processName, LocalTime startTime) {
        new AuthorizationWebPage().loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .openProcess(processName.getValue(), BankRussiaProcessColumn.PLANNED_START_TIME,
                        startTime.format(ofPattern(XML_TIME_FORMAT.getValue()))
                );
        return this;
    }

    @Step("Ожидание выполнения шагов")
    public AuctionSteps waitStepsFinished(List<String> steps, int timeOut) {
        BankRussiaPage page = new BankRussiaPage();
        if (steps.isEmpty()) {
            page.waitAllStepsFinished(timeOut);
            SpvbUtils.takeScreenshot();
        }
        for (String step : steps) {
            page.filter(step)
                    .waitAllStepsFinished(timeOut);
            SpvbUtils.takeScreenshot();
        }
        return this;
    }

    @Step("Проверить названия всех шагов в процессе")
    public AuctionSteps checkListStepsOfProcess(List<String> steps) {
        new BankRussiaPage()
                .checkListStepsOfProcess(steps);
        return this;
    }

    @Step("Проверить файл {namePfx13}")
    public AuctionSteps checkPfx13Tag(String namePfx13, boolean isBoardExist,
                                      Map<String, String> pfx13TabAttrs, Map<String, String> boardTabAttrs) {
        log.info("Проверить файл {}", namePfx13);
        downloadFile(namePfx13,
                BR_OUT_NO_SIGN.getValue(),
                TEMP_FILES.getValue());
        Document doc = XmlUtils.parseXml(TEMP_FILES.getValue() + namePfx13);
        Node node = doc.selectSingleNode("//PFX13_TAB");
        SoftAssertions softly = new SoftAssertions();

        checkXmlElementAttrs(node, pfx13TabAttrs, softly, "//PFX13_TAB");

        SpvbUtils.step("Проверка наличия тега <BOARD>");
        softly.assertThat(!doc.selectNodes("//BOARD").isEmpty())
                .describedAs("Наличие тега <BOARD> в файле")
                .isEqualTo(isBoardExist);
        if (isBoardExist) {
            node = doc.selectSingleNode("//BOARD");
            checkXmlElementAttrs(node, boardTabAttrs, softly, "//PFX13_TAB");
        }
        softly.assertAll();
        return this;
    }

    @Step("Проверить файл {pff63Name}")
    public AuctionSteps checkPff63Tag(String pff63Name, Map<String, String> pff63TabAttrs,
                                      List<List<Map<String, String>>> attrs, List<String> expectedAttrs) {
        log.info("Проверить файл {}", pff63Name);
        downloadFile(pff63Name,
                BR_OUT_NO_SIGN.getValue(),
                TEMP_FILES.getValue());
        Document doc = XmlUtils.parseXml(TEMP_FILES.getValue() + pff63Name);
        Node node = doc.selectSingleNode("//PFF63_TAB");
        SoftAssertions softAssertions = new SoftAssertions();
        checkXmlElementAttrs(node, pff63TabAttrs, softAssertions, "//PFF63_TAB");
        checkSumValue(doc, softAssertions);
        List<Node> list = doc.selectNodes("//BOARD/GROUP/PFF63_REC");

        for (List<Map<String, String>> attr : attrs) {
            checkAttrsCount(attr, list, "PFF63_REC", attr.get(1).get("SECURITYID"));
        }

        for (List<Map<String, String>> attr : attrs) {
            list.stream()
                    .filter(n -> n.getParent().getParent().selectSingleNode("@BOARDID").getText().equals(attr.get(0).get("BOARDID")) &&
                            n.getParent().selectSingleNode("@SECURITYID").getText().equals(attr.get(1).get("SECURITYID")) &&
                            n.selectSingleNode("@QUANTITY").getText().equals(attr.get(2).get("QUANTITY")))
                    .forEach(n -> {
                        checkXmlElementAttrs(n.getParent().getParent(), attr.get(0), softAssertions, "BOARD", attr.get(1).get("SECURITYID"));
                        checkXmlElementAttrs(n.getParent(), attr.get(1), softAssertions, "GROUP", attr.get(1).get("SECURITYID"));
                        checkXmlElementAttrs(n, attr.get(2), softAssertions, "PFF63_REC", attr.get(1).get("SECURITYID"));
                        checkXmlAttrsExist(n, expectedAttrs, softAssertions, "PFF63_REC", attr.get(1).get("SECURITYID"));
                    });
        }


        softAssertions.assertAll();
        return this;
    }

    public AuctionSteps checkFinalPfx13(String namePfx13) {
        downloadFile(namePfx13,
                BR_OUT_NO_SIGN.getValue(),
                TEMP_FILES.getValue());
        Document doc = XmlUtils.parseXml(TEMP_FILES.getValue() + namePfx13);
        String xpathTemplate = "//BOARD[@BOARDID='%s']/PFX13_REC[contains(@REPOVALUE, '%s') and @CURRENCY='%s']";
        String msg = "Проверить наличие BOARDID='%s' с тегом внутри [<PFX13_REC REPOVALUE=%s, CURRENCY=%s ...>]";
        SoftAssertions softly = new SoftAssertions();
        List<String> args = List.of("RRYX", "1000000.00", "CNY");
        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("RRYX", "2000000.00", "CNY");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("LRRX", "6000000.00", "RUB");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("LRRX", "5000000.00", "RUB");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("RRAX", "1000000.00", "RUB");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();

        args = List.of("RRAX", "20000000.00", "RUB");
        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("VRVX", "10000000.00", "RUB");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("VRVX", "2000000.00", "RUB");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("FRFX", "10000000.00", "RUB");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();
        args = List.of("FRFX", "20000000.00", "RUB");

        softly.assertThat(doc.selectSingleNode(String.format(xpathTemplate, args.toArray())))
                .describedAs(String.format(msg, args.toArray())).isNotNull();

        softly.assertAll();
        return this;
    }

    public AuctionSteps checkXmlAttrs(Node node, Map<String, String> attrs, SoftAssertions softly, String tagName) {
        String errMsg = "Не верно значение [%s]";
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            SpvbUtils.step(String.format("Проверить, что поле [%s] равно [%s]", entry.getKey(), entry.getValue()));
            softly.assertThat(node.selectSingleNode("@" + entry.getKey()).getText())
                    .describedAs(String.format(errMsg, entry.getKey()))
                    .isEqualTo(entry.getValue());
        }
        return this;
    }

    @Step("Проверить атрибуты xml файла в теге '{tagName}'")
    public AuctionSteps checkXmlElementAttrs(Node node, Map<String, String> attrs, SoftAssertions softly, String tagName) {
        return checkXmlAttrs(node, attrs, softly, tagName);
    }

    @Step("Проверить атрибуты в теге '{tagName}' для бумаги '{stockCode}'")
    public AuctionSteps checkXmlElementAttrs(Node node, Map<String, String> attrs, SoftAssertions softly,
                                             String tagName, String stockCode) {
        return checkXmlAttrs(node, attrs, softly, tagName);
    }

    @Step("Проверить количество тегов '{tagName}' для бумаги '{stockCode}'")
    public AuctionSteps checkAttrsCount(List<Map<String, String>> attr, List<Node> list, String tagName, String stockCode) {
        long actualCount = list.stream()
                .filter(n -> n.getParent().getParent().selectSingleNode("@BOARDID").getText().equals(attr.get(0).get("BOARDID")) &&
                        n.getParent().selectSingleNode("@SECURITYID").getText().equals(attr.get(1).get("SECURITYID")))
                .count();
        long expectedCount = Long.parseLong(attr.get(3).get("PFF63_COUNT"));
        SpvbUtils.step(String.format("Проверить, что количество тегов '%s' для бумаги '%s' равно %d",
                tagName, stockCode, expectedCount));
        Assertions.assertThat(actualCount)
                .describedAs(String.format("Проверить, что количество тегов '%s' для бумаги '%s' равно %d",
                        tagName, stockCode, expectedCount))
                .isEqualTo(expectedCount);
        return this;
    }

    @Step("Проверить наличие аттрибутов тега '{tagName}'")
    public AuctionSteps checkXmlAttrsExist(Node node, List<String> expectedAttrs, SoftAssertions softly, String tagName, String stockCode) {
        for (String entry : expectedAttrs) {
            SpvbUtils.step(String.format("Проверить, что в теге [%s] для бумаги [%s] присутствует аттрибут [%s]", tagName, stockCode, entry));
            softly.assertThat(node.selectSingleNode("@" + entry))
                    .describedAs(String.format("Проверить, что в теге [%s] для бумаги [%s] присутствует аттрибут [%s]", tagName, stockCode, entry))
                    .isNotNull();
        }
        return this;
    }

    @Step("Проверить, что значение атрибута SUM_VALUE в теге PFF63_TAB равно сумме всех значений атрибутов VALUE из тегов BOARD")
    public AuctionSteps checkSumValue(Document doc, SoftAssertions softly) {
        int actualSumValue = 0;
        List<Node> list = doc.selectNodes("//BOARD/GROUP/PFF63_REC/@VALUE");
        for (Node node : list) {
            actualSumValue += Integer.parseInt(node.getText().substring(0, node.getText().lastIndexOf('.')));
        }
        int expectedSumValue = Integer.parseInt(doc.selectSingleNode("//PFF63_TAB/@SUM_VALUE").getText().substring(0,
                doc.selectSingleNode("//PFF63_TAB/@SUM_VALUE").getText().lastIndexOf('.')));

        log.info(String.format("Проверить, что поле [SUM_VALUE] равно [%d]", actualSumValue));
        softly.assertThat(actualSumValue)
                .describedAs("Проверить, что значение атрибута SUM_VALUE в теге PFF63_TAB равно сумме всех значений атрибутов VALUE из тегов BOARD")
                .isEqualTo(expectedSumValue);
        return this;
    }

    @Step("Добавить время сбора заявок")
    public AuctionSteps setGetAppsTime(Map<String, Boolean> times) {
        if (!BasePage.isLoggedIn())
            AuthorizationWebPage.getInstance().loginUser();
        BankRussiaPage.getInstance().open(MenuTab.BANK_RUSSIA, BankRussiaPage.class).setAppGetTime(times);
        return this;
    }

    @Step("Отправить .xml файл и переименовать")
    public AuctionSteps sendXmlAndRename(String fileName) {
        copyFile(TEMP_FILES.getValue() + fileName);
        signFileBrIn(fileName);
        renameFile(BR_IN.getValue(), fileName + SIG.getValue(), fileName + P7S.getValue());
        return this;
    }

    @Step("Открыть процесс {processName}")
    public AuctionSteps openProcess(String processName) {
        if (!BasePage.isLoggedIn())
            AuthorizationWebPage.getInstance().loginUser();
        Selenide.refresh();
        BankRussiaPage.getInstance().open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .openProcess(processName);
        return this;
    }

    @Step("Войти в quik")
    public AuctionSteps loginQuik(FilePath appPath) {
        if (appPath.equals(FilePath.QUIK_SBER) || appPath.equals(QUIK_AB_RUSSIA) || appPath.equals(QUIK_CB))
            authorizationQuikPage.loginCb(appPath);
        else
            authorizationQuikPage.loginBank(appPath);
        return this;
    }

    @Step("Открыть меню создания таблицы торгов")
    public AuctionSteps openCreateTradesTableWindow() {
        new CurrentTradesPage().openCreateTableWindow();
        return this;
    }

    @Step("Проверить возможность создать таблицу текущих торгов с инструментом {instrument}")
    public AuctionSteps checkCurrentTradeExist(String instrument, boolean isExist) {
        new CurrentTradesPage().checkTradesContainsExist(instrument, isExist);
        DesktopUtils.takeScreenshot();
        return this;
    }

    @Step("Создать заявку РЕПО в квике")
    public AuctionSteps createAppRepo(String instrument, Map<String, String> data) {
        new SearchInstrumentsPage()
                .openSearchInstrument()
                .searchInstrument(instrument)
                .openInstrumentQuotes()
                .openCreateAppPage()
                .createApp(data)
                .checkLastDialogueMessageContains("зарегистрирована", "Проверить, что заявка успешно зарегистрирована");
        return this;
    }

    @Step("Создать файл PFX12")
    public AuctionSteps createPfx12(String fileNamePfx12, String stepName, Map<Double, String> amountStatus) {
        new BankRussiaPage().filter(stepName);
        String pfx11Name = SpvbUtils.generateFileNameSaveIndex(PFX11, XML_P7S, BR_OUT);

        SpvbUtils.step(String.format("Скачать файл [%s] из br/out", pfx11Name));
        downloadFile(pfx11Name, BR_OUT.getValue(), TEMP_FILES.getValue());
        Document spcexDoc = SpvbUtils.getNodeFromP7sFile(TEMP_FILES.getValue() + pfx11Name, "SPCEX_DOC");
        Element board = ((Element) spcexDoc.selectSingleNode("//BOARD")).createCopy();
        Document pfx12 = XmlUtils.parseXml(FILE_TEMPLATES_DIR.getValue() + PFX12 + XML.getValue());

        ((Element) pfx12.selectSingleNode("//PFX12_TAB")).add(board);

        addStatusAttrPfx12(board, amountStatus);
        pfx12.selectSingleNode("//PFX12_TAB/@TRADEDATE")
                .setText(LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue())));
        XmlUtils.toXml(pfx12, TEMP_FILES.getValue() + fileNamePfx12);
        return this;
    }

    @SuppressWarnings("rawtypes")
    public AuctionSteps addStatusAttrPfx12(Element board, Map<Double, String> attrs) {
        List nodes = board.selectNodes("//PFX11_REC");
        for (Object node : nodes) {
            Element el = (Element) node;
            el.setName("PFX12_REC");
            Double amount = Double.valueOf(el.selectSingleNode("@AMOUNT1").getText());
            el.addAttribute("STATUS", attrs.get(amount));
        }
        return this;
    }

    @Step("Открыть дочерний процесс")
    public AuctionSteps openChildProcess(String parentProcess, String childProcess) {
        if (!BasePage.isLoggedIn())
            AuthorizationWebPage.getInstance().loginUser().open(MenuTab.BANK_RUSSIA, BankRussiaPage.class);
        BankRussiaPage page = BankRussiaPage.getInstance();
        page.clearProcessFilter()
                .openChildProcess(parentProcess, childProcess);
        return this;
    }

    @Step("Ожидание завершения дочерних процессов")
    public AuctionSteps checkAucChildProcessesFinished(String aucName, List<String> childProcesses) {
        if (!BasePage.isLoggedIn())
            AuthorizationWebPage.getInstance().loginUser().open(MenuTab.BANK_RUSSIA, BankRussiaPage.class);
        BankRussiaPage page = BankRussiaPage.getInstance();
        for (String process : childProcesses) {
            Selenide.refresh();
            page.clearProcessFilter()
                    .filterProcesses(process)
                    .openChildProcess(aucName, process)
                    .waitAllStepsFinished(5 * 60);
        }
        return this;
    }

    public AuctionSteps auction() {

        return this;
    }

    @Step("Провести аукцион по инструменту с торговым кодом '{stockCode}'")
    public AuctionSteps holdAuction(String stockName, String stockCode, String clientCode, Map<String, String> priceQuantity,
                                    int expectedNumberOfTransactionsComplete) {
        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(PFX41, "START_TIME", stockCode));
        loginQuik(QUIK_VTB);
        sendRequestByQuik(stockName, priceQuantity, clientCode);
        SpvbUtils.cleanQuikFiles();

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(PFX41, "START_CUT_TIME", stockCode));
        String pfx38Name = SpvbUtils.generateFileNameSaveIndex(PFX38, XML, BR_OUT_NO_SIGN);
        String pfx39Name = SpvbUtils.generateFileNameIncreaseIndex(PFX39, XML, BR_IN);

        checkPfx38Status(stockCode);
        if (priceQuantity.size() > 2) {
            createPfx39_t113(pfx38Name, pfx39Name);
        } else {
            createPfx39_t87(pfx38Name, pfx39Name);
        }
        copyFile(FilePath.TEMP_FILES.getValue() + pfx39Name)
                .signFile(BR_IN.getValue() + pfx39Name)
                .renameFile(BR_IN.getValue(), pfx39Name + SIG.getValue(), pfx39Name + P7S.getValue())
                .checkPfx39Status(pfx39Name.substring(0, pfx39Name.lastIndexOf('.')), stockCode)
                .downloadAuctionCbrFile()
                .prepareAuctionCbrTriFile(stockCode)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .checkNumberOfTransactionsComplete(expectedNumberOfTransactionsComplete)
                .closeDesktop()
                .checkSuccessAuctionStatus(stockCode)
                .closeWebDriver();
        return this;
    }

    public AuctionSteps startExtraReplacement(FileName pfx, String stockCode) {
        String pfx42Name = SpvbUtils.generateFileNameIncreaseIndex(PFX42, XML, BR_IN);
        createPfx42(pfx, pfx42Name)
                .copyFile(TEMP_FILES.getValue() + pfx42Name)
                .signFile(BR_IN.getValue() + pfx42Name)
                .renameFile(BR_IN.getValue(), pfx42Name + SIG.getValue(), pfx42Name + P7S.getValue());

        WaitingUtils.sleep(2 * 60);
        checkPfx42Status(PFX42_GET_FROM_BR.getValue(), pfx42Name.substring(0, pfx42Name.lastIndexOf('.')))
                .checkExtraReplacementsExist(pfx, EXTRA_REPLACEMENT_TEXT.getValue())
                .checkProcessStartTime(pfx42Name, stockCode)
                .closeWebDriver();
        return this;
    }

    @Step("Провести доразмещение по инструменту с торговым кодом '{stockCode}'")
    public AuctionSteps holdExtraReplacement(String stockCode, int expectedNumberOfTransactionsComplete,
                                             FileName pfx42, boolean isOutStockExchange, String clientCode) {
        startExtraReplacement(pfx42, stockCode)
                .extraReplacement(stockCode, expectedNumberOfTransactionsComplete, pfx42);
        if (isOutStockExchange) {
            sendOutStockExchangeRequests(stockCode, pfx42, clientCode, expectedNumberOfTransactionsComplete);
        }
        closeDesktop();
        return this;
    }

    @Step("Провести доразмещение по инструменту с торговым кодом '{stockCode}'")
    public AuctionSteps holdExtraReplacement(String stockCode, int expectedNumberOfTransactionsComplete, FileName pfx42_1,
                                             FileName pfx42_2, boolean isOutStockExchange, String clientCode) {
        startExtraReplacement(pfx42_1, stockCode)
                .startExtraReplacement(pfx42_2, stockCode)
                .extraReplacement(stockCode, expectedNumberOfTransactionsComplete, pfx42_2);
        if (isOutStockExchange) {
            sendOutStockExchangeRequests(stockCode, pfx42_2, clientCode, expectedNumberOfTransactionsComplete);
        }
        closeDesktop();
        return this;
    }

    public AuctionSteps extraReplacement(String stockCode, int expectedNumberOfTransactionsComplete, FileName pfx) {
        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(pfx, "START_TIME", stockCode));

        checkPfx43RequestStatus(EXTRA_REPLACEMENT_TEXT.getValue() + stockCode, PFX43_SEND_TO_BR.getValue());
        String pfx43RemoteName = SpvbUtils.generateFileNameSaveIndex(PFX43, XML, BR_OUT_NO_SIGN);
        String pfx43IncreaseIndexName = SpvbUtils.generateFileNameIncreaseIndex(PFX43, XML, BR_OUT_NO_SIGN);
        createPfx43(pfx43RemoteName, pfx43IncreaseIndexName)
                .copyFile(TEMP_FILES.getValue() + pfx43IncreaseIndexName)
                .signFile(BR_IN.getValue() + pfx43IncreaseIndexName)
                .renameFile(BR_IN.getValue(), pfx43IncreaseIndexName + SIG.getValue(),
                        pfx43IncreaseIndexName + P7S.getValue())
                .checkPfx43MonitoringStatus(PFX43_MONITORING.getValue(), pfx43IncreaseIndexName.substring(0, pfx43IncreaseIndexName.lastIndexOf('.')))
                .checkPfx43ExportQuikStatus(PFX43_EXPORT_QUIK.getValue(), EXTRA_REPLACEMENT_TEXT.getValue() + stockCode, PFX43_SEND_TO_BR.getValue())
                .downloadExtraPlacementCbrFile()
                .closeWebDriver()
                .prepareExtraReplacementCbrTriFile(stockCode)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .checkNumberOfTransactionsComplete(expectedNumberOfTransactionsComplete)
                .closeDesktop();
        return this;
    }

    @Step("Проверить, что процесс [{process}] завершен")
    public AuctionSteps waitPfx12GetFromBrFinish(String process, String pfx12Name) {
        if (!BasePage.isLoggedIn())
            new AuthorizationWebPage().loginUser().open(MenuTab.BANK_RUSSIA, BankRussiaPage.class);
        BankRussiaPage page = BankRussiaPage.getInstance();
        Selenide.refresh();

        page.clearProcessFilter()
                .filterProcesses(process)
                .expandProcessName("Мониторинг PFX12")
                .waitProcessWithStepFinished(process, pfx12Name, 5 * 60);
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Ожидание наступления времени {time}")
    public AuctionSteps waitTime(LocalTime time) {
        WaitingUtils.waitUntil(LocalDateTime.of(LocalDate.now(), time));
        return this;
    }

    @Step("Создать окно [{window}] в QUIK")
    public AuctionSteps createQuikWindow(String window) {
        new NewWindowPage()
                .searchWindow(window)
                .chooseWindow(window);
        return this;
    }

    @Step("Проверить, наличие столбца [{columnName}] со значением [{expectedColumnVal}] в таблице")
    public AuctionSteps checkColumnValueExist(String table, String columnName, String columnTag,
                                              String expectedColumnVal, boolean isExist) {
        List<String> rows = List.of(table.split("\n"));
        List<String> columnCells = new ArrayList<>();
        for (String row : rows) {
            List<String> cells = List.of(row.split(";"));
            List<String> cc = cells.stream().filter(c -> c.contains(columnTag))
                    .map(c -> c.split(" ")[3]).toList();
            columnCells.addAll(cc);
        }

        ListAssert<String> listAssert = Assertions.assertThat(columnCells)
                .describedAs(String.format("Проверить наличие в таблице столбца %s со значением %s", columnName, expectedColumnVal));

        if (isExist)
            listAssert.contains(expectedColumnVal);
        else {
            listAssert.doesNotContain(expectedColumnVal);
        }
        return this;
    }

    @Step("Проверить статус процесса в модуле регламентных операций")
    public AuctionSteps checkProcessStatusRoutineOps(String processName, String groupName) {
        AuthorizationWebPage.getInstance().loginUser()
                .open(MenuTab.ROUTINE_OPERATIONS, RoutineOperationsPage.class)
                .filterProcesses(processName)
                .expandProcessName(groupName)
                .checkProcessStatus(processName, COMPLETE.getValue());
        SpvbUtils.takeScreenshot();
        return this;
    }

    public AuctionSteps checkProcessStatus(String processName, Status expected) {
        AuthorizationWebPage.getInstance().loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .filterProcesses(processName);

        BankRussiaPage.getInstance().checkProcessStatus(processName, expected.getValue());
        SpvbUtils.takeScreenshot();
        return this;
    }

    @Step("Проверить, что входящий остаток равен {expected}")
    public AuctionSteps checkIncomingBalance(String expected, boolean isExist) {
        loginQuik(QUIK_PSB);
        createQuikWindow("Позиции по деньгам");
        String fileToSave = "money_positions.txt";
        new MoneyPositionsPage().saveTableData(fileToSave);
        String tableData = SpvbUtils.readFromFile(TEMP_FILES.getValue() + fileToSave);
        checkColumnValueExist(tableData, "Входящий остаток", "OPEN_BALANCE", expected, isExist);
        closeDesktop();
        return this;
    }

    public AuctionSteps sendPfx09AndCheckIncomingBalance(String fileNamePfx, Map<XmlAttribute, String> data) {
        sendXmlAndRename(fileNamePfx);
        openProcess(PFX09_NOTIFICATION.getValue());
        waitStepsFinished(List.of(fileNamePfx.substring(0, fileNamePfx.indexOf("."))), 10 * 60);
        closeWebDriver();
        checkIncomingBalance(new DecimalFormat(BIG_DOUBLE.getValue()).format(Double.parseDouble(data.get(LIMIT))), true);
        return this;
    }

    public AuctionSteps waitStepWithColumn(String stepName, BankRussiaStepsColumn column, String expectedVal) {
        BankRussiaPage.getInstance().waitStepWithColumn(stepName, column, expectedVal);
        return this;
    }

    @Step("Загрузить позиции из .lim файла")
    public AuctionSteps loadLimFileQuik(FilePath filePath) {
        File file = new File(filePath.getValue());
        BaseQuikPage page = new BaseQuikPage();
        page.openMainMenuTab(QuikMainMenu.BROKER);
        page.chooseMainMenuTabItem(QuikMainMenuItems.UPLOAD_POSITIONS_FROM_FILE);
        page.pathData().setText(file.getAbsolutePath());
        page.okButton().click();
        DesktopUtils.takeScreenshot();
        page.okButton().click();
        DesktopUtils.takeScreenshot();
        return this;
    }

    public LocalDateTime getTimeFromUi(String stockCode, BankRussiaProcessColumn column) {
        LocalDateTime result = AuthorizationWebPage.getInstance().loginUser()
                .open(MenuTab.BANK_RUSSIA, BankRussiaPage.class)
                .filterProcesses(String.format("Аукцион (торговый код бумаги: %s)", stockCode))
                .getTimeFromUi(String.format("Аукцион (торговый код бумаги: %s)", stockCode), column);
        closeWebDriver();
        return result;
    }

    @Step("Открыть вкладку {tab}")
    public AuctionSteps loginWeb(MenuTab tab) {
        AuthorizationWebPage.getInstance().loginUser()
                .open(tab);
        return this;
    }


    @Step("Проверить, что процесс '{processName}' перейдет в статус '{status.value}'")
    public AuctionSteps checkProcessStatus(String processName, Status status, LocalDateTime plannedStartTime) {
        BankRussiaPage.getInstance().waitProcessStatus(processName,
                String.format("Ожидание, что процесс '%s' перейдет в статус '%s'", processName, status.getValue()),
                status, plannedStartTime);
        return this;
    }

    @Step("Проверить, что установлен режим работы {operatingMode}")
    public AuctionSteps checkOperatingMode(String processName, OperatingMode operatingMode, LocalDateTime startTime) {
        BankRussiaPage.getInstance().checkOperatingMode(processName, operatingMode, startTime);
        return this;
    }

    @Step("Проверить, что уведомления содержат сообщение '{message}', отправленное после наступления времени '{startTime}'")
    public AuctionSteps checkNotificationMessage(String message, LocalDateTime startTime) {
        BankRussiaPage.getInstance()
                .checkNotificationMessage(message, startTime)
                .closeNotifications();
        return this;
    }


    @Step("Проверить, что в папке br/in на SFTP отсутствуют файлы")
    public AuctionSteps checkBrInEmpty() {
        List<String> list = new SftpUtils().getAllFileNamesInDirectory(BR_IN.getValue());
        List<String> excluded = List.of(".", "..", ".m2", "PFX09");
        Assertions.assertThat(list)
                .describedAs("Проверить, что в папке br/in на SFTP отсутствуют файлы")
                .containsExactlyInAnyOrderElementsOf(excluded);
        return this;
    }

    @Step("Проверить, что в папке br/out на SFTP отсутствуют файлы")
    public AuctionSteps checkBrOutEmpty() {
        List<String> list = new SftpUtils().getAllFileNamesInDirectory(BR_OUT.getValue());
        List<String> excluded = List.of(".", "..", "tmp");
        Assertions.assertThat(list)
                .describedAs("Проверить, что в папке br/in на SFTP отсутствуют файлы")
                .containsExactlyInAnyOrderElementsOf(excluded);
        return this;
    }

    @Step("Открыть вкладку 'Аудит', установить дату и время '{startTime}'")
    public AuctionSteps openAudit(LocalDateTime startTime) {
        new BasePage()
                .open(MenuTab.AUDIT, AuditPage.class)
                .addOperationNameFilter()
                .setCurrentDate(startTime);
        return this;
    }

    @Step("Установить фильтр по наименованию операции '{operationName}'")
    public AuctionSteps findOperation(String operationName) {
        AuditPage.getInstance()
                .setOperationName(operationName)
                .find();
        return this;
    }

    @Step("Проверить, что все события за текущий день в результате операции = '{allowedStatuses}'")
    public AuctionSteps checkAuditStatus(List<String> allowedStatuses) {
        AuditPage.getInstance()
                .checkAllPagesByStatus(allowedStatuses);
        return this;
    }

    @Step("Проверить, что в описании событитя отображается информация '{message}'")
    public AuctionSteps checkAuditEventDescription(String message, String operationName) {
        AuditPage.getInstance()
                .checkLastEventDescription(message, operationName);
        return this;
    }

    public AuctionSteps checkPfx64_65(String processName, String operationName, LocalDateTime startTime, String message) {
        loginWeb(MenuTab.BANK_RUSSIA)
                .checkProcessStatus(processName, Status.IN_PROGRESS, startTime)
                .checkProcessStatus(processName, Status.ERROR, startTime)
                .checkOperatingMode(processName, OperatingMode.MANUAL, startTime)
                .checkNotificationMessage(message, startTime)
                .openAudit(startTime)
                .findOperation(operationName)
                .checkAuditStatus(List.of(Status.START.getValue(), Status.ERROR.getValue()))
                .checkAuditEventDescription(message, operationName);
        return this;
    }

    @Step("Проверить, что шаги процесса '{processName}' с 1 по '{countSteps}' находятся в статусе '{status}'")
    public AuctionSteps waitProcessCountStepsFinished(String processName, String containsText, int countSteps, String status) {
        BankRussiaPage.getInstance()
                .waitCountStepsFinished(containsText, countSteps, status,
                        () -> BankRussiaPage.getInstance()
                                .openProcess(processName));
        return this;
    }
}
