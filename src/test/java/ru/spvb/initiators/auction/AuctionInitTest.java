package ru.spvb.initiators.auction;

import constants.Credentials;
import constants.DateFormat;
import constants.DocStatus;
import constants.FilePath;
import db.initiators.auctions.AuctionInitDbHelper;
import elements.columns.AuctionInitStandardColumn;
import elements.columns.InitDocRegistry;
import io.qameta.allure.Epic;
import io.qameta.allure.Link;
import lombok.SneakyThrows;
import model.dbo.LimitsDbo;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.dom4j.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import page.quik.AuthorizationQuikPage;
import page.quik.RequestPage;
import page.web.initiators.auction.*;
import ru.spvb.auction.common.BaseTest;
import ru.spvb.steps.FilterSteps;
import ru.spvb.steps.auction.AuctionSteps;
import ru.spvb.steps.auctionInitSteps.AuctionInitSteps;
import utils.PfxUtils;
import utils.SftpUtils;
import utils.WaitingUtils;
import utils.XmlUtils;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static constants.DateFormat.*;
import static constants.FilePath.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static page.web.initiators.InitiatorsPage.FilterColumns.*;
import static page.web.initiators.auction.OpenAucOrTradeHeaderPage.GeneralStatus.DECLINED;
import static utils.SpvbUtils.currTimePlusMin;
import static utils.SpvbUtils.generateMkrFileName;

@Order(2)
@Epic("Инициаторы: аукционы")
public class AuctionInitTest extends BaseTest {

    @Test
    @Order(0)
    @DisplayName("Загрузить позиции из файла для тестов раздела 'Инициаторы: аукционы'")
    public void loadAuctionInitPositionsFromFile() {
        Credentials.setEnv("test");
        new AuctionSteps()
                .loginQuik(FilePath.QUIK_SBER)
                .loadLimFileQuik(FilePath.AUCTION_LIMITS_LIM);
        new AuthorizationQuikPage().checkLastDialogueMessageContains("загружено",
                "Проверить, что файл с позициями по инициаторам аукционам успешно загружен");

    }

    @Test
    @Order(10)
    @Link(name = "SPB-T152", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T152")
    @DisplayName("SPB-T152. АТС Смольного - АУКЦИОН заявки НЕ ПОДАНЫ, сделок нет DSTAA10S031")
    public void SPB_T152Test() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DSTAA10S031");
        new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.STANDARD)
                .createAuc("ГУП", new File("src/test/resources/empty_pdf.pdf"))
                .openSiteGoToInitAuc()
                .createAuc("ГУП", new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900", "today", "31", "Открытый",
                        currTimePlusMin(3, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(5, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(8, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DSTAA10SX6X7X8", "XMDT - ATSS", "today", "RUB", "5");
        LocalDateTime time = LocalDateTime.now().plusMinutes(8);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DSTAA10S031")
                .waitUntil(time)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .hasNoApplications()
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_589_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(DECLINED);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DSTAA10S031"));
    }

    @Test
    @Order(5)
    @Link(name = "SPB-T251", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T251")
    @DisplayName("SPB-T251. КФ СПБ - ТОРГИ: заявки не поданы, сделок нет DT1000S002U")
    public void SPB_T251Test() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DT1000S002U");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFSPB)
                .createTrade(DepositAuctionNTradesPage.TradeType.AUCTION, new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.TRADE))
                .fillTrade("B0", "today", "2", "Открытый", "Фиксированная ставка",
                        currTimePlusMin(3, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(5, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(8, PROCESSING_APP_TIME_FORMAT),
                        "DT1000SX6X7X8U", "XMNT", "today", "RUB", "По ставке" +
                                " Уполномоченного банка", "5", "900");
        LocalDateTime time = LocalDateTime.now().plusMinutes(8);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DT1000S002U")
                .waitUntil(time)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .hasNoApplications()
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_302_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.DECLINED);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DT1000S002U"));

    }

    @Test
    @Order(6)
    @Link(name = "SPB-T293", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T293")
    @DisplayName("SPB-T293. КФ СПБ - АУКЦИОН: заявки поданы, сделки есть____________ DT1100S859U")
    public void SPB_T293Test() {
        Credentials.setEnv("test");
        String stockCode = "DT1100S859U";
        int reqFrom = 2;
        int reqTo = 5;
        int raseTo = 8;
        int bidTo = 11;

        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount(stockCode) + 1;

        AuctionInitSteps steps = new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFSPB)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "1000000.66", "today", "859", "Открытый",
                        currTimePlusMin(reqFrom, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(reqTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(bidTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DT1100SX6X7X8U", "XMDT - KFSP", "today", "RUB", "1")
                .setSatisfyingApplications("По ставке Уполномоченного банка")
                .setRateIncreaseTime(currTimePlusMin(reqTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(raseTo, DateFormat.PROCESSING_APP_TIME_FORMAT))
                .setTypeOfDepositRate("Постоянная ставка");

        LocalDateTime requestFrom = LocalDateTime.now().plusMinutes(reqFrom);
        LocalDateTime requestTo = LocalDateTime.now().plusMinutes(reqTo);
        LocalDateTime raiseBidTo = LocalDateTime.now().plusMinutes(raseTo);

        steps
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .waitUntil(requestFrom);

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("14");
        list2.add("1000000000");
        List<String> list3 = new ArrayList<>();
        list3.add("14");
        list3.add("660");
        list.add(list2);
        list.add(list3);

        steps
                .refreshBroker()
                .loginQuikCreateApl(QUIK_SBER, stockCode, list)
                .waitUntil(requestTo)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(1), "15")
                .closeDesktop();

        List<String> change2 = new ArrayList<>();
        change2.add("15");
        change2.add("660");
        list.remove(1);
        list.add(1, change2);

        List<List<String>> requests = new ArrayList<>();
        List<String> request = new ArrayList<>();
        request.add("14");
        request.add("1000000660");
        requests.add(request);

        steps
                .waitUntil(raiseBidTo)

                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .sendApplicationsAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .loginQuikCreateApl(QUIK_FINANCE_SPB, stockCode, requests)
                .closeDesktop()

                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(list, "ПАО \"Сбербанк\"")
                .getChangedToUpdate()
                .hasSendTradesButton()
                .sendTradesAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()

                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_302_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealVolumeAccess()
                .checkAverageRateAccess()

                .fillRealVolumeAndAverageRates("1000000660", "14.1")
                .sendAuctionResults()

                .checkLastProcessFinishedInRoutineOps("Проведение депозитного аукциона (Код БИ: DT1100S859U)");

        WaitingUtils.sleep(20);
        Assertions.assertThat(steps.getAucCount(stockCode)).isEqualTo(count);
    }


    @Test
    @Order(16)
    @Link(name = "SPB-T273", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T273")
    @DisplayName("SPB-T273. ФСКМСБ - аукцион: заявки поданы, сделки есть___________________DM1000S790 ///14:37 - 14:58")
    public void SPB_T273Test() {
        Credentials.setEnv("test");
        String stockCode = "DM1000S790";
        int reqFrom = 2;
        int reqTo = 5;
        int raseTo = 8;
        int bidTo = 11;

        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount(stockCode) + 1;

        AuctionInitSteps steps = new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FSKMSB)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900", "today", "790", "Открытый",
                        currTimePlusMin(reqFrom, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(reqTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(bidTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DM1000SX6X7X8", "XMDT - FSKB", "today", "RUB", "0.01")
                .setRateIncreaseTime(currTimePlusMin(reqTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(raseTo, DateFormat.PROCESSING_APP_TIME_FORMAT));

        LocalDateTime requestFrom = LocalDateTime.now().plusMinutes(reqFrom);
        LocalDateTime requestTo = LocalDateTime.now().plusMinutes(reqTo);
        LocalDateTime raiseBidTo = LocalDateTime.now().plusMinutes(raseTo);


        steps
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .waitUntil(requestFrom);

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("13");
        list2.add("20000");
        list.add(list2);

        steps
                .refreshBroker()
                .loginQuikCreateApl(QUIK_SBER, stockCode, list)
                .waitUntil(requestTo)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "14")
                .raiseBid("15")
                .closeDesktop();

        List<String> change = new ArrayList<>();
        change.add("15");
        change.add("20000");
        list.remove(0);
        list.add(0, change);

        steps
                .waitUntil(raiseBidTo)

                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .sendApplicationsAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_495_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .loginQuikCreateApl(QUIK_FSKMB, stockCode, list)
                .closeDesktop()

                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(list, "ПАО \"Сбербанк\"")
                .getChangedToUpdate()
                .hasSendTradesButton()
                .sendTradesAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_495_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()

                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_495_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealVolumeAccess()
                .checkAverageRateAccess()

                .fillRealVolumeAndAverageRates("1000000660", "14.1")
                .sendAuctionResults()

                .checkLastProcessFinishedInRoutineOps(String.format("Проведение депозитного аукциона (Код БИ: %s)", stockCode));

        WaitingUtils.sleep(20);
        Assertions.assertThat(steps.getAucCount(stockCode)).isEqualTo(count);
    }

    @Test
    @Order(19)
    @Link(name = "SPB-T270", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T270")
    @DisplayName("SPB-T270. ФК- аукцион: заявки поданы, сделки есть_________________________DK1000K006R ///15:51 - 16:25")
    public void SPB_T270Test() {
        Credentials.setEnv("test");
        File file = new AuctionInitSteps().generateXmlSpb_T270();
        String id = ((Element) XmlUtils.parseXml(file).selectSingleNode("//KDX10_REC")).attributeValue("AUCTION_ID");
        String stockCode = "DK1000K006R";
        String bankName = "ПАО \"Сбербанк\"";
        String quikBankName = "Депозиты ФК";
        int start1 = 3;
        int middle1 = 6;
        int end2 = 9;

        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount(stockCode) + 1;

        AuctionInitSteps steps = new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FK)
                .createAuc(file)
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("xml", AuctionInitDbHelper.Type.AUCTION));

        LocalDateTime startTime1 = LocalDateTime.now().plusMinutes(start1);
        LocalDateTime middleTime1 = LocalDateTime.now().plusMinutes(middle1);
        LocalDateTime endTime2 = LocalDateTime.now().plusMinutes(end2);

        steps
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.NEW)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .uploadFileParams(new File("src/test/resources/empty_pdf.pdf"))
                .uploadFileParams(new File("src/test/resources/AuctionInit/empty.docx"))
                .checkParamsDocGen(3, Pattern.compile("MKR_K_AUCTION_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())) + "_\\d{3}.(xml|pdf|docx)"))
                .export()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .waitUntil(startTime1);

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("19");
        list2.add("50000");
        list.add(list2);

        steps
                .refreshBroker()
                .loginQuikCreateApl(QUIK_SBER, quikBankName, stockCode, list)
                .waitUntil(middleTime1)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "19,50")
                .closeDesktop();

        List<String> change = new ArrayList<>();
        change.add("19.50");
        change.add("50000");
        list.remove(0);
        list.add(0, change);

        List<List<String>> requests = new ArrayList<>();
        List<String> request = new ArrayList<>();
        request.add("19,50");
        request.add("50000");
        requests.add(request);

        steps
                .waitUntil(endTime2)

                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK);

        List<String> applicationsBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");

        steps.sendApplications();

        Callable<Boolean> callable = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable);
        List<String> applicationsAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        applicationsAfter.removeAll(applicationsBefore);

        steps
                .checkApplicationDocGen(3, Pattern.compile("FKZRF_FTD(02|08|09)_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.xml"))
                .checkSftp(applicationsAfter,
                        Pattern.compile("SPCEX_DOC_DS_550_FKORDERS_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"));

        steps
                .loginQuikCreateApl(QUIK_FK, quikBankName, stockCode, requests)
                .closeDesktop()

                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(list, bankName)
                .getChangedToUpdate()
                .hasSendTradesButton();

        List<String> tradesBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");

        steps.sendTrades();

        Callable<Boolean> callable2 = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable2);
        List<String> tradesAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        tradesAfter.removeAll(tradesBefore);
        steps
                .checkTradesDocGen(1, Pattern.compile("FKZRF_FTD06_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) +
                        "_\\d{4}.xml"))
                .checkSftp(tradesAfter,
                        Pattern.compile("SPCEX_DOC_DS_550_FKTRADES_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .setResUrl("https://smth.com")
                .saveRes()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .publish()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COMPLETED)
                .checkDocumentStatus(DocStatus.SEND)

                .checkLastProcessFinishedInRoutineOps(String.format("Проведение депозитного аукциона (Код БИ: %s)", stockCode));

        WaitingUtils.sleep(20);
        Assertions.assertThat(steps.getAucCount(stockCode)).isEqualTo(count);
    }

    @Test
    @Order(11)
    @Link(name = "SPB-T258", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T258")
    @DisplayName("SPB-T258. АТС Смольного - АУКЦИОН заявки поданы, сделки ЕСТЬ_________________DSTAA10S033 ///12:25 - 12:52")
    public void SPB_T258Test() {
        Credentials.setEnv("test");
        String stockCode = "DSTAA10S033";
        int reqFrom = 2;
        int reqTo = 5;
        int raseTo = 7;
        int bidTo = 10;
        String bankName = "ПАО \"Сбербанк\"";
        String quikBankName = "Депозиты \"АТС Смольного\"";

        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount(stockCode) + 1;

        AuctionInitSteps steps = new AuctionInitSteps()
                .createAuc("ГУП", new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900000", "today", "33", "Открытый",
                        currTimePlusMin(reqFrom, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(reqTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(bidTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DSTAA10SX6X7X8", "XMDT - ATSS", "today", "RUB", "0.01")
                .setRateIncreaseTime(currTimePlusMin(reqTo, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(raseTo, DateFormat.PROCESSING_APP_TIME_FORMAT));

        LocalDateTime requestFrom = LocalDateTime.now().plusMinutes(reqFrom);
        LocalDateTime requestTo = LocalDateTime.now().plusMinutes(reqTo);
        LocalDateTime raiseBidTo = LocalDateTime.now().plusMinutes(raseTo);


        steps
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .waitUntil(requestFrom);

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("10");
        list2.add("10000000");
        list.add(list2);

        steps
                .refreshBroker()
                .loginQuikCreateApl(QUIK_SBER, quikBankName, stockCode, list)
                .waitUntil(requestTo)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "13")
                .raiseBid("14")
                .closeDesktop();

        List<String> change = new ArrayList<>();
        change.add("14");
        change.add("10000000");
        list.remove(0);
        list.add(0, change);

        steps
                .waitUntil(raiseBidTo)

                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(list, bankName)
                .hasSendApplicationsButton()
                .sendApplicationsAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .loginQuikCreateApl(QUIK_SMOLNOGO, quikBankName, stockCode, list)
                .closeDesktop()

                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(list, bankName)
                .getChangedToUpdate()
                .hasSendTradesButton()
                .sendTradesAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()

                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_589_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealVolumeAccess()
                .checkAverageRateAccess()

                .fillRealVolumeAndAverageRates("10000000000", "14")
                .sendAuctionResults()

                .checkLastProcessFinishedInRoutineOps(String.format("Проведение депозитного аукциона (Код БИ: %s)", stockCode));

        WaitingUtils.sleep(20);
        Assertions.assertThat(steps.getAucCount(stockCode)).isEqualTo(count);
    }

    @Test
    @Order(4)
    @Link(name = "SPB-T298", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T298")
    @DisplayName("SPB-T298. КФ СПБ - ТОРГИ: заявки поданы, сделки есть _________DT1000S001U + Редактирование объявления + Обновление в ТС (1-1)///10:00 - 10:30")
    public void SPB_T298Test() {
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(0).withSecond(0));
        Credentials.setEnv("test");
        String stockCode = "DT1000S001U";
        String bankName = "ПАО \"Сбербанк\"";

        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount(stockCode) + 1;

        AuctionInitSteps steps = new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFSPB)
                .createTrade(DepositAuctionNTradesPage.TradeType.AUCTION, new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.TRADE))

                .fillTrade("B0", "today", "1", "Открытый", "Фиксированная ставка",
                        "1005", "1006", "1010",
                        "DT1000SX6X7X8U", "XMNT - KFSP", "today", "RUB", "По ставке" +
                                " Уполномоченного банка", "5", "9999999")
                .setRateIncreaseTime("1006", "1008")
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)

                .edit()
                .changeTime("1010", "1013", "1013", "1015", "1018")
                .saveParams()
                .updateParams();

        LocalDateTime requestFrom = LocalDateTime.now().plusMinutes(10);
        LocalDateTime requestTo = LocalDateTime.now().plusMinutes(13);
        LocalDateTime raiseBidTo = LocalDateTime.now().plusMinutes(15);

        steps
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .waitUntil(requestFrom);

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("15");
        list2.add("50000000");
        list.add(list2);

        steps
                .refreshBroker()
                .loginQuikCreateApl(QUIK_SBER, stockCode, list)
                .waitUntil(requestTo)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "16")
                .closeDesktop();

        List<String> change = new ArrayList<>();
        change.add("16");
        change.add("50000000");
        list.remove(0);
        list.add(0, change);

        List<List<String>> webList = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        list3.add("16");
        list3.add("50000");
        webList.add(list3);

        steps
                .waitUntil(raiseBidTo)

                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(webList, bankName)
                .hasSendApplicationsButton()
                .sendApplicationsAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .loginQuikCreateApl(QUIK_FINANCE_SPB, stockCode, list)
                .closeDesktop()

                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(webList, bankName)
                .getChangedToUpdate()
                .hasSendTradesButton()
                .sendTradesAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()

                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_302_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealVolumeAccess()
                .checkAverageRateAccess()

                .fillRealVolumeAndAverageRates("50000000", "16")
                .sendAuctionResults();

        WaitingUtils.sleep(20);
        Assertions.assertThat(steps.getAucCount(stockCode)).isEqualTo(count);
    }

    @Test
    @Order(1)
    @Link(name = "SPB-T147", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T147")
    @DisplayName("SPB-T147. Фильтры поиска: вкладка \"Стандартные инициаторы\"")
    public void SPB_T147() {
        Credentials.setEnv("test");
        AuctionInitDbHelper dbHelper = new AuctionInitDbHelper();

        List<LimitsDbo> auctionsWithMask = dbHelper.getAuctionsWithNameTemplate("MKR_AA_DTRADE_%%_%%.%%");

        String exchangeInstrument = "DSTAA10S003";

        LocalDateTime from = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(1);
        LocalDateTime to = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        String status = "Завершен";

        String initiator = "СПб ГУП \"АТС Смольного\"";

        SoftAssertions softly = new SoftAssertions();
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.STANDARD);
        new FilterSteps()
                .setFilter(NAME_DOC, List.of(auctionsWithMask.get(0).getName()))
                .checkColumnHasSameStrings(AuctionInitStandardColumn.NAME_DOC, List.of(auctionsWithMask.get(0).getName()), softly)
                .refreshPage()
                .setFilter(EXCHANGE_INSTRUMENT, List.of(exchangeInstrument))
                .checkFilteredColumn(AuctionInitStandardColumn.EXCHANGE_INSTRUMENT, exchangeInstrument, softly)
                .refreshPage()
                .setFilter(DATE, List.of(
                        from.format(ofPattern(XML_DATE_FORMAT.getValue())),
                        to.format(ofPattern(XML_DATE_FORMAT.getValue())))
                ).checkFilteredDatesBetween(
                        AuctionInitStandardColumn.DATE_PROCESSING,
                        from, to, WEB_DATE_TIME_FORMAT, softly
                ).refreshPage()
                .setFilter(STATUS, List.of(status))
                .checkFilteredColumn(AuctionInitStandardColumn.STATUS, status, softly)
                .refreshPage()
                .setFilter(INITIATOR_CONTRIBUTOR_CODE, List.of(initiator))
                .checkFilteredColumn(AuctionInitStandardColumn.INITIATOR_CONTRIBUTOR_CODE, initiator, softly);
        softly.assertAll();
    }

    @Test
    @Order(2)
    @Link(name = "SPB-T148", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T148")
    @DisplayName("SPB-T148. Фильтры поиска: вкладка \"Реестр документов\"")
    public void SPB_T148() {
        Credentials.setEnv("test");
        AuctionInitDbHelper dbHelper = new AuctionInitDbHelper();
        SoftAssertions softly = new SoftAssertions();

        List<String> repoWithMask = dbHelper.getAuctionsWithNameTemplate("REPO_LO_ACCEPT_%%_%%.%%")
                .stream().map(a -> a.name).toList();

        LocalDateTime from = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(1);
        LocalDateTime to = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        String status = "Отклонен";
        String type = "Объявление об аукционе";
        String initiator = "Комитет финансов СПб";
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSiteGoToDocRegistry();
        new FilterSteps()
                .setFilter(NAME_DOC, repoWithMask)
                .checkColumnHasSameStrings(InitDocRegistry.NAME_DOC, List.of(repoWithMask.get(0)), softly)
                .setFilter(DATE, List.of(
                        from.format(ofPattern(XML_DATE_FORMAT.getValue())),
                        to.format(ofPattern(XML_DATE_FORMAT.getValue())))
                ).checkFilteredDatesBetween(
                        InitDocRegistry.DATE_PROCESSING,
                        from, to, WEB_DATE_TIME_FORMAT, softly
                )
                .setFilter(STATUS, List.of(status))
                .checkFilteredColumn(InitDocRegistry.STATUS, status, softly)
                .setFilter(TYPE, List.of(type))
                .checkFilteredColumn(InitDocRegistry.TYPE, type, softly)
                .setFilter(INITIATOR_CONTRIBUTOR_CODE, List.of(initiator))
                .checkFilteredColumn(InitDocRegistry.INITIATOR_CONTRIBUTOR_CODE, initiator, softly);
        softly.assertAll();
    }

    @SneakyThrows
    @Test
    @Order(18)
    @Link(name = "SPB-T268", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T268")
    @DisplayName("SPB-T268. ФК- аукцион: заявки не поданы, сделок нет DK1000K023R")
    public void SPB_T268() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DK1000K023R");
        File file = new AuctionInitSteps().generateXmlSpb_T268();
        LocalDateTime time = LocalDateTime.now().plusMinutes(14);
        String id = ((Element) XmlUtils.parseXml(file).selectSingleNode("//KDX10_REC")).attributeValue("AUCTION_ID");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FK)
                .createAuc(file)
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("xml", AuctionInitDbHelper.Type.AUCTION))
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.NEW)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .uploadFileParams(new File("src/test/resources/empty_pdf.pdf"))
                .uploadFileParams(new File("src/test/resources/AuctionInit/empty.docx"))
                .checkParamsDocGen(3, Pattern.compile("MKR_K_AUCTION_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())) + "_\\d{3}.(xml|pdf|docx)"))
                .export()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .waitUntil(time)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoApplications();
        List<String> applicationsBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendApplications();
        Callable<Boolean> callable = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable);
        List<String> applicationsAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        applicationsAfter.removeAll(applicationsBefore);
        new AuctionInitSteps()
                .checkApplicationDocGen(3, Pattern.compile("FKZRF_FTD(02|08|09)_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.xml"))
                .checkSftp(applicationsAfter,
                        Pattern.compile("SPCEX_DOC_DS_550_FKORDERS_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .getChangedToUpdate()
                .hasNoTrades()
                .hasSendTradesButton();
        List<String> tradesBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendTrades();
        Callable<Boolean> callable2 = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable2);
        List<String> tradesAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        tradesAfter.removeAll(tradesBefore);
        new AuctionInitSteps()
                .checkTradesDocGen(1, Pattern.compile("FKZRF_FTD06_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) +
                        "_\\d{4}.xml"))
                .checkSftp(tradesAfter,
                        Pattern.compile("SPCEX_DOC_DS_550_FKTRADES_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .setResUrl("https://smth.com")
                .saveRes()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .publish()
                .checkGeneralStatus(DECLINED)
                .checkDocumentStatus(DocStatus.SEND);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DK1000K023R"));
    }

    @Test
    @Order(33)
    @Link(name = "SPB-T287", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T287")
    @DisplayName("SPB-T287. Автоматическое отклонение аукциона ФК, если в XML прошлая дата")
    public void SPB_T287() {
        Credentials.setEnv("test");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FK)
                .createAuc(new AuctionInitSteps().generateXmlSpb_T287())
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("xml", AuctionInitDbHelper.Type.AUCTION))
                .checkGeneralStatus(DECLINED)
                .checkDocumentStatus(DocStatus.ERROR);
    }

    @Test
    @Order(34)
    @Link(name = "SPB-T286", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T286")
    @DisplayName("SPB-T286. Автоматическое отклонение аукциона ФК, если ошибка XML")
    public void SPB_T286() {
        Credentials.setEnv("test");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FK)
                .createAuc(new File("src/test/resources/AuctionInit/invalid.xml"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("xml", AuctionInitDbHelper.Type.AUCTION))
                .checkGeneralStatus(DECLINED)
                .checkDocumentStatus(DocStatus.ERROR);
    }

    @Test
    @Order(27)
    @Link(name = "SPB-T292", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T292")
    @DisplayName("SPB-T292. АО «Автоградбанк»: аукцион объявление, когда не допущен ни один УТ")
    public void SPB_T292() {
        Credentials.setEnv("test");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .createAuc("Автоградбанк", new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900", "today", "31", "Открытый",
                        currTimePlusMin(3, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(5, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(9, PROCESSING_APP_TIME_FORMAT),
                        "DSTAE10SX6X7X8", "XMDT - AVGB", "today", "RUB", "5")
                .saveParams()
                .checkAutofill("DSTAE10S031")
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .export()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.ERROR);
    }

    @Test
    @Order(14)
    @Link(name = "SPB-T255", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T255")
    @DisplayName("SPB-T255. ВЭБ РФ - аукцион: заявки НЕ ПОДАНЫ, сделок нет DVEB10S007")
    public void SPB_T255() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DVEB10S007");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAucNoCur("B0", "900", "today", "7", "Открытый",
                        currTimePlusMin(3, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(5, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(9, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DVEB10SX6X7X8", "XMXX - VBRF", "today", "0.01");
        LocalDateTime time = LocalDateTime.now().plusMinutes(9);
        new AuctionInitSteps()
                .setReturnDate(LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())))
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.NEW)
                .checkDocumentStatus(DocStatus.NEW)
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkAutofill("DVEB10S007")
                .exportEditDeclineExists()
                .export()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .waitUntil(time)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoApplications();
        List<String> applicationsBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendApplications();
        Callable<Boolean> callable = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable);
        List<String> applicationsAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        applicationsAfter.removeAll(applicationsBefore);
        new AuctionInitSteps()
                .checkApplicationDocGen(3, Pattern.compile("VEBRF_\\d{4}_VTD(02|09|08)_[678]_"
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.FILE_DATE_FORMAT.getValue())) + ".xml"))
                .checkSftp(applicationsAfter,
                        Pattern.compile("SPCEX_DOC_DS_594_IORDERS_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .getChangedToUpdate()
                .hasNoTrades()
                .hasSendTradesButton();
        List<String> tradesBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendTrades();
        Callable<Boolean> callable2 = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable2);
        List<String> tradesAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        tradesAfter.removeAll(tradesBefore);
        new AuctionInitSteps()
                .checkTradesDocGen(2, Pattern.compile("(SPCEX_DOC_DS_594_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) +
                        "_\\d{5}.xlsx|VEBRF_\\d{4}_VTD06_10_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.FILE_DATE_FORMAT.getValue())) +
                        ".xml)"))
                .checkSftp(tradesAfter,
                        Pattern.compile("SPCEX_DOC_DS_594_ITRADES_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .openSection(OpenAucOrTradeHeaderPage.Section.NRD, new OpenedAucOrTradeNRDPage())
                .getNRDStatus()
                .checkNRDDownloadExists()
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.NEW)
                .setResUrl("https://smth.com")
                .saveRes()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .publish()
                .checkGeneralStatus(DECLINED)
                .checkDocumentStatus(DocStatus.SEND);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DVEB10S007"));
    }

    @Test
    @Order(17)
    @Link(name = "SPB-T269", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T269")
    @DisplayName("SPB-T269. ФК- аукцион: заявки поданы, сделок нет DK1000K005R")
    public void SPB_T269() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DK1000K005R");
        File file = new AuctionInitSteps().generateXmlSpb_T269();
        String id = ((Element) XmlUtils.parseXml(file).selectSingleNode("//KDX10_REC")).attributeValue("AUCTION_ID");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FK)
                .createAuc(file)
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("xml", AuctionInitDbHelper.Type.AUCTION));
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(8);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(14);
        new AuctionInitSteps()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.NEW)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .uploadFileParams(new File("src/test/resources/empty_pdf.pdf"))
                .uploadFileParams(new File("src/test/resources/AuctionInit/empty.docx"))
                .checkParamsDocGen(3, Pattern.compile("MKR_K_AUCTION_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())) + "_\\d{3}.(xml|pdf|docx)"))
                .export()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("16");
        list2.add("10000");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты ФК", "DK1000K005R", list)
                .waitUntil(time2)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK);
        List<String> applicationsBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendApplications();
        Callable<Boolean> callable = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable);
        List<String> applicationsAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        applicationsAfter.removeAll(applicationsBefore);
        new AuctionInitSteps()
                .checkApplicationDocGen(3, Pattern.compile("FKZRF_FTD(02|08|09)_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.xml"))
                .checkSftp(applicationsAfter,
                        Pattern.compile("SPCEX_DOC_DS_550_FKORDERS_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .waitUntil(time3)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .getChangedToUpdate()
                .hasNoTrades()
                .hasSendTradesButton();
        List<String> tradesBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendTrades();
        Callable<Boolean> callable2 = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable2);
        List<String> tradesAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        tradesAfter.removeAll(tradesBefore);
        new AuctionInitSteps()
                .checkTradesDocGen(1, Pattern.compile("FKZRF_FTD06_" + id + "_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) +
                        "_\\d{4}.xml"))
                .checkSftp(tradesAfter,
                        Pattern.compile("SPCEX_DOC_DS_550_FKTRADES_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .setResUrl("https://smth.com")
                .saveRes()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .publish()
                .checkGeneralStatus(DECLINED)
                .checkDocumentStatus(DocStatus.SEND);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DK1000K005R"));
    }

    @Test
    @Order(9)
    @Link(name = "SPB-T257", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T257")
    @DisplayName("SPB-T257. АТС Смольного - АУКЦИОН заявки поданы, сделок НЕТ DSTAA10S032")
    public void SPB_T257() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DSTAA10S032");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .createAuc("ГУП", new File("src/test/resources/Limits/test1_1.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900", "today", "32", "Открытый",
                        currTimePlusMin(3, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(7, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(11, PROCESSING_APP_TIME_FORMAT),
                        "DSTAA10SX6X7X8", "XMDT - ATSS", "today", "RUB", "1");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(7);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(11);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DSTAA10S032")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("11");
        list2.add("100000");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты \"АТС Смольного\"", "DSTAA10S032", list)
                .waitUntil(time2)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .waitUntil(time3)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_589_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(DECLINED);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DSTAA10S032"));
    }


    @Test
    @Order(28)
    @Link(name = "SPB-T276", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T276")
    @DisplayName("SPB-T276. ВЭБ РФ - аукцион: заявки поданы, сделок НЕТ DVEB10S008")
    public void SPB_T276() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DVEB10S008");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAucNoCur("B0", "900", "today", "8", "Открытый",
                        currTimePlusMin(3, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(7, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(11, PROCESSING_APP_TIME_FORMAT),
                        "DVEB10SX6X7X8", "XMXX - VBRF", "today", "0.01")
                .setReturnDate(LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())));
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(7);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(11);
        new AuctionInitSteps()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.NEW)
                .checkDocumentStatus(DocStatus.NEW)
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkAutofill("DVEB10S008")
                .exportEditDeclineExists()
                .export()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("20");
        list2.add("60000");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозтиты \"ВЕБ.РФ\"", "DVEB10S008", list)
                .waitUntil(time2)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(list, "ПАО \"Сбербанк\"");
        List<String> applicationsBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendApplications();
        Callable<Boolean> callable = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable);
        List<String> applicationsAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        applicationsAfter.removeAll(applicationsBefore);
        new AuctionInitSteps()
                .checkApplicationDocGen(3, Pattern.compile("VEBRF_\\d{4}_VTD(02|09|08)_[678]_"
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.FILE_DATE_FORMAT.getValue())) + ".xml"))
                .checkSftp(applicationsAfter,
                        Pattern.compile("SPCEX_DOC_DS_594_IORDERS_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .waitUntil(time3)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .getChangedToUpdate()
                .hasNoTrades()
                .hasSendTradesButton();
        List<String> tradesBefore = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        new AuctionInitSteps()
                .sendTrades();
        Callable<Boolean> callable2 = () -> applicationsBefore.size() != new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/").size();
        WaitingUtils.waitUntil(10, 1, 1, "Файлы появились на сфтп", callable2);
        List<String> tradesAfter = new SftpUtils().getAllFileNamesInDirectory("/report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/ZIP/");
        tradesAfter.removeAll(tradesBefore);
        new AuctionInitSteps()
                .checkTradesDocGen(2, Pattern.compile("(SPCEX_DOC_DS_594_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) +
                        "_\\d{5}.xlsx|VEBRF_\\d{4}_VTD06_10_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.FILE_DATE_FORMAT.getValue())) +
                        ".xml)"))
                .checkSftp(tradesAfter,
                        Pattern.compile("SPCEX_DOC_DS_594_ITRADES_" +
                                LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue()))
                                + "_\\d{5}.zip"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .openSection(OpenAucOrTradeHeaderPage.Section.NRD, new OpenedAucOrTradeNRDPage())
                .getNRDStatus()
                .checkNRDDownloadExists()
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.NEW)
                .setResUrl("https://smth.com")
                .saveRes()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .publish()
                .checkGeneralStatus(DECLINED)
                .checkDocumentStatus(DocStatus.SEND);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DVEB10S008"));
    }

    @Test
    @Order(20)
    @Link(name = "SPB-T274", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T274")
    @DisplayName("SPB-T274. ФАУ ГГЭ - аукцион: заявки поданы, сделок нет DG1000S055")
    public void SPB_T274() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DG1000S055");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FAUGGE)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900", "today", "55", "Открытый",
                        currTimePlusMin(3, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(7, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(11, PROCESSING_APP_TIME_FORMAT),
                        "DG1000SX6X7X8", "XMDS - FGGE", "today", "RUB", "0.01")
                .setTypeOfDepositRate("Постоянная ставка");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(7);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(11);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DG1000S055")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("17");
        list2.add("30000");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты ФАУ ГГЭ", "DG1000S055", list)
                .waitUntil(time2)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_575_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .waitUntil(time3)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_575_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_575_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(DECLINED);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DG1000S055"));
    }

    @Test
    @Order(15)
    @Link(name = "SPB-T272", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T272")
    @DisplayName("SPB-T272. ФСКМСБ - аукцион: заявки поданы, сделок нет DM1000S789")
    public void SPB_T272() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DM1000S789");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FSKMSB)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900", "today", "789", "Открытый",
                        currTimePlusMin(3, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(7, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(11, PROCESSING_APP_TIME_FORMAT),
                        "DM1000SX6X7X8", "XMDT - FSKB", "today", "RUB", "0.01");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(7);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(11);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DM1000S789")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("11");
        list2.add("10000");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты ФСКМБ", "DM1000S789", list)
                .waitUntil(time2)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_495_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .waitUntil(time3)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_495_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_495_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(DECLINED);
        Assertions.assertThat(count + 1).isEqualTo(new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DM1000S789"));
    }

    @Test
    @Order(31)
    @Link(name = "SPB-T297", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T297")
    @DisplayName("SPB-T297. Автоматическое отклонение аукциона КФ ЛО по завершении дня (после экспорта в ТС, статус - Сбор заявок) DL1000S0017")
    public void SPB_T297() {
        Credentials.setEnv("test");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFLO)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "1000", "today", "17", "Открытый",
                        currTimePlusMin(3, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(7, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(16, PROCESSING_APP_TIME_FORMAT),
                        "DL1000S0X6X7X8", "XMDS - KFLO", "today", "RUB", "1")
                .setRateIncreaseTime(currTimePlusMin(7, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(11, PROCESSING_APP_TIME_FORMAT))
                .setTypeOfDepositRate("Постоянная ставка");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(11);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DL1000S0017")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("10");
        list2.add("1000");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты КФ ЛО", "DL1000S0017", list)
                .waitUntil(time2)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK);
    }

    @Test
    @Order(30)
    @Link(name = "SPB-T295", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T295")
    @DisplayName("SPB-T295. Автоматическое отклонение торгов КФ СПБ по завершении дня (До экспорта в ТС, статус - Новый)")
    public void SPB_T295() {
        Credentials.setEnv("test");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFSPB)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.NEW)
                .checkDocumentStatus(DocStatus.NEW);
    }

    @SneakyThrows
    @Test
    @Order(32)
    @Link(name = "SPB-T290", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T290")
    @DisplayName("SPB-T290. Автоматическое отклонение аукциона ФК по завершении дня (До экспорта в ТС, статус - В работе) DK1000K023R")
    public void SPB_T290() {
        Credentials.setEnv("test");
        File file = new AuctionInitSteps().generateXmlSpb_T268();
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FK)
                .createAuc(file)
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("xml", AuctionInitDbHelper.Type.AUCTION))
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.NEW)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .uploadFileParams(new File("src/test/resources/empty_pdf.pdf"))
                .uploadFileParams(new File("src/test/resources/AuctionInit/empty.docx"))
                .checkParamsDocGen(3, Pattern.compile("MKR_K_AUCTION_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())) + "_\\d{3}.(xml|pdf|docx)"));
    }

    @Test
    @Order(26)
    @Link(name = "SPB-T296", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T296")
    @DisplayName("SPB-T296. Условия проведения аукциона: 1)кол-во заявок, 2)мин. сумма 3)мин. % ставка 4)автомат. отклонение из статуса \"Сбор сделок\" для АТС Смольного DSTAA10S035")
    public void SPB_T296() {
        Credentials.setEnv("test");
        List<List<List<String>>> data = new AuctionInitSteps().getApllSPB_T296();
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DSTAA10S035");
        new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.STANDARD)
                .setMinSumCon("400000")
                .setMaxAplCon("4")
                .createAuc("ГУП", new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "9999999", "today", "35", "Открытый",
                        currTimePlusMin(3, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(7, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(11, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DSTAA10SX6X7X8", "XMDT - ATSS", "today", "RUB", "10");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(7);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(11);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DSTAA10S035")
                .waitUntil(time1);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateAplError(FilePath.QUIK_SBER, "Депозиты \"АТС Смольного\"", "DSTAA10S035", data.get(0), "меньше минимальной ставки")
                .createAplError(data.get(1), "меньше минимального объема заявки")
                .createApl(data.get(2))
                .createAplError(data.get(3), "больше допустимого количества заявок от одного участника")
                .waitUntil(time2)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkApl(data.get(2), "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .waitUntil(time3)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_589_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_589_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(DECLINED);
        Assertions.assertThat(count + 1)
                .as("Проверить, что во вкладке 'Регламентные операции' аукцион отображается и имеет статус 'Завершен'")
                .isEqualTo(new AuctionInitSteps()
                        .openSiteGoToInitAuc()
                        .getAucCount("DSTAA10S035"));
    }

    @Test
    @Order(8)
    @Link(name = "SPB-T317", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T317")
    @DisplayName("SPB-T317. КФ ЛО - АУКЦИОН: заявки поданы, сделок нет DL1000S0011///11:23 - 11:40")
    public void SPB_T317() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DL1000S0011");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFLO)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "1000000.667", "today", "11", "Открытый",
                        currTimePlusMin(3, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(7, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(11, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DL1000S0X6X7X8", "XMDS - KFLO", "today", "RUB", "1")
                .setTypeOfDepositRate("Постоянная ставка");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(7);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(11);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DL1000S0011")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        list2.add("14");
        list2.add("1000000");
        list3.add("14");
        list3.add("667");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты КФ ЛО", "DL1000S0011", list)
                .waitUntil(time2)
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_574_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .waitUntil(time3)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_574_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_574_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(DECLINED);
        Assertions.assertThat(count + 1)
                .as("Проверить, что во вкладке 'Регламентные операции' аукцион отображается и имеет статус 'Завершен'")
                .isEqualTo(new AuctionInitSteps()
                        .openSiteGoToInitAuc()
                        .getAucCount("DL1000S0011"));
    }

    @Test
    @Order(7)
    @Link(name = "SPB-T294", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T294")
    @DisplayName("SPB-T294. +КФ ЛО - АУКЦИОН: заявки поданы, сделки ЕСТЬ_______DL1000S0010__(1к1000)///11:05 - 11:22")
    public void SPB_T294() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DL1000S0010");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFLO)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "1000000.667", "today", "10", "Открытый",
                        currTimePlusMin(3, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(6, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(12, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DL1000S0X6X7X8", "XMDS - KFLO", "today", "RUB", "1")
                .setTypeOfDepositRate("Постоянная ставка")
                .setRateIncreaseTime(currTimePlusMin(6, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(9, DateFormat.PROCESSING_APP_TIME_FORMAT));
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(6);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(9);
        LocalDateTime time4 = LocalDateTime.now().plusMinutes(12);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DL1000S0010")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("14");
        list2.add("1000000");
        list.add(list2);
        List<String> list3 = new ArrayList<>();
        list3.add("14");
        list3.add("667");
        list.add(list3);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты КФ ЛО", "DL1000S0010", list)
                .waitUntil(time2)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "16")
                .closeDesktop()
                .waitUntil(time3);
        list.remove(0);
        List<String> change = new ArrayList<>();
        change.add("16");
        change.add("1000000");
        list.add(change);
        new AuctionInitSteps()
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_574_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"));
        List<List<String>> listt = new ArrayList<>();
        List<String> list22 = new ArrayList<>();
        list22.add("16");
        list22.add("1000000");
        listt.add(list22);
        new AuctionInitSteps()
                .loginQuikCreateApl(QUIK_FINANCE_LO, "Депозиты КФ ЛО", "DL1000S0010", listt)
                .waitUntil(time4)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(listt, "ПАО \"Сбербанк\"")
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_574_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_574_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkRealVolumeAccess()
                .checkAverageRateAccess()

                .fillRealVolumeAndAverageRates("1000000000", "16")
                .sendAuctionResults()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COMPLETED);
        Assertions.assertThat(count + 1)
                .as("Проверить, что во вкладке 'Регламентные операции' аукцион отображается и имеет статус 'Завершен'")
                .isEqualTo(new AuctionInitSteps()
                        .openSiteGoToInitAuc()
                        .getAucCount("DL1000S0010"));
    }

    @Test
    @Order(21)
    @Link(name = "SPB-T275", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T275")
    @DisplayName("SPB-T275. +ФАУ ГГЭ - аукцион: заявки поданы, сделки есть______________DG1000S056 ///16:48 - 17:12")
    public void SPB_T275() {
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DG1000S056");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.FAUGGE)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAuc("B0", "900", "today", "56", "Открытый",
                        currTimePlusMin(3, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(6, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(12, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        "DG1000SX6X7X8", "XMDS - FGGE", "today", "RUB", "0.01")
                .setTypeOfDepositRate("Постоянная ставка")
                .setRateIncreaseTime(currTimePlusMin(6, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(9, DateFormat.PROCESSING_APP_TIME_FORMAT));
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(6);
        LocalDateTime time3 = LocalDateTime.now().plusMinutes(9);
        LocalDateTime time4 = LocalDateTime.now().plusMinutes(12);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DG1000S056")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("9");
        list2.add("40000");
        list.add(list2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Депозиты ФАУ ГГЭ", "DG1000S056", list)
                .waitUntil(time2)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "13")
                .raiseBid("16")
                .closeDesktop()
                .waitUntil(time3);
        list.remove(0);
        List<String> change = new ArrayList<>();
        change.add("16");
        change.add("40000");
        list.add(change);
        new AuctionInitSteps()
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkApl(list, "ПАО \"Сбербанк\"")
                .hasSendApplicationsButton()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .sendApplicationsAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_575_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"));
        List<List<String>> listt = new ArrayList<>();
        List<String> list22 = new ArrayList<>();
        list22.add("16");
        list22.add("40000");
        listt.add(list22);
        new AuctionInitSteps()
                .loginQuikCreateApl(QUIK_FAU_GGE, "Депозиты ФАУ ГГЭ", "DG1000S056", list)
                .waitUntil(time4)
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .hasSendTradesButton()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(list, "ПАО \"Сбербанк\"")
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_575_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()
                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_575_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkRealVolumeAccess()
                .checkAverageRateAccess()

                .fillRealVolumeAndAverageRates("40000000", "16")
                .sendAuctionResults()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COMPLETED);
        Assertions.assertThat(count + 1)
                .as("Проверить, что во вкладке 'Регламентные операции' аукцион отображается и имеет статус 'Завершен'")
                .isEqualTo(new AuctionInitSteps()
                        .openSiteGoToInitAuc()
                        .getAucCount("DG1000S056"));
    }

    @Test
    @Order(25)
    @Link(name = "SPB-T325", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T325")
    @DisplayName("SPB-T325. +КФ СПБ - Инициатор против всех: заявки поданы, сделок нет_____DT1000S033U_///18:28 - 18:43")
    public void SPB_T325(){
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DT1000S033U");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFSPB)
                .createTrade(DepositAuctionNTradesPage.TradeType.INITIATOR_AGAINST_ALL , new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.TRADE))
                .fillInitAgainstAll("33",  currTimePlusMin(3, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(10, DateFormat.PROCESSING_APP_TIME_FORMAT), "DT1000SX6X7X8U",
                        "XMTT - KFSP", "RUB", "900000");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(3);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(10);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DT1000S033U")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        List<String> list4 = new ArrayList<>();
        list2.add("10");
        list2.add("50000");
        list3.add("11");
        list3.add("50000");
        list4.add("15");
        list4.add("50000");
        list.add(list2);
        list.add(list3);
        list.add(list4);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Торги ИПВ КФ СПБ", "DT1000S033U", list)
                .waitUntil(time2)
                .closeDesktop();
        new AuctionInitSteps()
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasSendTradesButton()
                .hasNoTrades()
                .sendTradesAllFormats()
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .checkGeneralStatus(DECLINED);
        Assertions.assertThat(count + 1)
                .as("Проверить, что во вкладке 'Регламентные операции' аукцион отображается и имеет статус 'Завершен'")
                .isEqualTo(new AuctionInitSteps()
                        .openSiteGoToInitAuc()
                        .getAucCount("DT1000S033U"));
    }

    @Test
    @Order(24)
    @Link(name = "SPB-T324", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T324")
    @DisplayName("SPB-T324. +КФ СПБ - Инициатор против всех: заявки поданы, сделки есть_____DT1000S010U_///17:58 - 18:24")
    public void SPB_T324(){
        Credentials.setEnv("test");
        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount("DT1000S010U");
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFSPB)
                .createTrade(DepositAuctionNTradesPage.TradeType.INITIATOR_AGAINST_ALL , new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.TRADE))
                .fillInitAgainstAll("10",  currTimePlusMin(1, DateFormat.PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(10, DateFormat.PROCESSING_APP_TIME_FORMAT), "DT1000SX6X7X8U",
                        "XMTT - KFSP", "RUB", "900000");
        LocalDateTime time1 = LocalDateTime.now().plusMinutes(1);
        LocalDateTime time2 = LocalDateTime.now().plusMinutes(10);
        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill("DT1000S010U")
                .waitUntil(time1);
        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        List<String> list4 = new ArrayList<>();
        list2.add("10");
        list2.add("50000");
        list3.add("11");
        list3.add("50000");
        list4.add("15");
        list4.add("50000");
        list.add(list2);
        list.add(list3);
        list.add(list4);
        List<List<String>> trades1 = new ArrayList<>();
        List<String> listt2 = new ArrayList<>();
        listt2.add("15");
        listt2.add("10000");
        trades1.add(listt2);
        new AuctionInitSteps()
                .refreshBroker()
                .loginQuikCreateApl(FilePath.QUIK_SBER, "Торги ИПВ КФ СПБ", "DT1000S010U", list)
                .closeDesktop()
                .loginQuikCreateApl(QUIK_FINANCE_SPB, "Торги ИПВ КФ СПБ", "DT1000S010U", trades1)
                .closeDesktop()
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkDocumentStatus(DocStatus.NEW)
                .getTrades()
                .getChangedToUpdate()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(trades1, "ПАО \"Сбербанк\"");
        List<List<String>> trades2 = new ArrayList<>();
        List<String> listt3 = new ArrayList<>();
        listt3.add("15");
        listt3.add("20000");
        trades2.add(listt3);
        trades1.add(listt3);
        new AuctionInitSteps()
                .loginQuikCreateApl(QUIK_FINANCE_SPB, "Торги ИПВ КФ СПБ", "DT1000S010U", trades2)
                .closeDesktop()
                .updateTrades()
                .checkRealApl(trades1, "ПАО \"Сбербанк\"")
                .hasSendTradesButton()
                .sendTradesAllFormats()
                .waitUntil(time2)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_302_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COMPLETED);
        Assertions.assertThat(count + 1)
                .as("Проверить, что во вкладке 'Регламентные операции' аукцион отображается и имеет статус 'Завершен'")
                .isEqualTo(new AuctionInitSteps()
                        .openSiteGoToInitAuc()
                        .getAucCount("DT1000S010U"));
    }

    @Test
    @Order(3)
    @Link(name = "SPB-T187", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T187")
    @DisplayName("SPB-T187. Экспорт инструментов МКР в Торговую систему")
    public void SPB_T187() {
        String fileName = generateMkrFileName(INITIATORS_MKR_SECURITIES);
        PfxUtils.createMkrSecExport(fileName);
        SftpUtils.uploadFile(Path.of(TEMP_FILES.getValue() + fileName), INITIATORS_MKR_SECURITIES.getValue());
        SoftAssertions softly = new SoftAssertions();
        new AuctionInitSteps()
                .waitMkrProcessFinish(fileName)
                .checkMkrDocExist(fileName, softly)
                .checkSecCodesInDb(fileName, softly)
                .deleteSecCodes(fileName);

        softly.assertAll();
    }

    @Test
    @Order(12)
    @Link(name = "SPB-T277", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T277")
    @DisplayName("SPB-T277. ВЭБ РФ - аукцион: заявки поданы, сделки есть ________(включая НРД: OK/DEAL и т.д.) ____________DVEB10S009")
    public void SPB_T277() {
        Credentials.setEnv("test");
        String sber = "ПАО \"Сбербанк\"";
        String stockCode = "DVEB10S009";

        LocalDateTime requestFrom = LocalDateTime.now().plusMinutes(2);
        LocalDateTime requestTo = LocalDateTime.now().plusMinutes(6);
        LocalDateTime requestBidPlusTo = LocalDateTime.now().plusMinutes(15);
        LocalDateTime raiseBidTo = LocalDateTime.now().plusMinutes(9);
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAucNoCur("B0", "900", "today", "9", "Открытый",
                        requestFrom.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        requestTo.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        requestBidPlusTo.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        "DVEB10SX6X7X8", "XMXX - VBRF", "today", "0.01")
                .setRateIncreaseTime(
                        requestTo.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        requestTo.plusMinutes(3).format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue()))
                ).setReturnDate(LocalDateTime.now().plusDays(9).format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())));

        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .refreshBroker()
                .waitUntil(requestFrom.plusSeconds(10));

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("20");
        list2.add("60000");
        list.add(list2);

        new AuctionInitSteps()
                .loginQuikCreateApl(QUIK_SBER, stockCode, list)
                .waitUntil(requestTo)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "20,2")
                .checkBid(20.2)
                .raiseBid("20,6")
                .checkBid(20.6)
                .closeDesktop();

        List<List<String>> requests = new ArrayList<>();
        List<String> request = new ArrayList<>();
        request.add("20.60");
        request.add("60000");
        requests.add(request);

        new AuctionInitSteps()
                .waitUntil(raiseBidTo.plusMinutes(1))
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(requests, sber)
                .hasSendApplicationsButton()
                .sendApplications()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .loginQuikCreateApl(QUIK_VEB, stockCode, requests)
                .closeDesktop()
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()
                .hasSendTradesButton()
                .sendTrades()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(2, Pattern.compile("(SPCEX_DOC_DS_594_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) +
                        "_\\d{5}.xlsx|VEBRF_\\d{4}_VTD06_10_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.FILE_DATE_FORMAT.getValue())) +
                        ".xml)"))
                .downloadAndCheckSentDealsNRD(Map.of(
                        "pay_sum", "60000000.00",
                        "deposit_rate", "20.60000"))
                .openSection(OpenAucOrTradeHeaderPage.Section.NRD, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.NEW)
                .getNRDStatus()
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkTableNrdRow("20.60", "NEW")
                .downloadAndCheckNrdAnswer("NEW")
                .waitUntil(LocalDateTime.now().plusMinutes(5).plusSeconds(10))
                .getNRDStatus()
                .checkTableNrdRow("20.60", "ERR")
                .downloadAndCheckNrdAnswer("ERR")
                .waitUntil(LocalDateTime.now().plusMinutes(5).plusSeconds(10))
                .getNRDStatus()
                .checkTableNrdRow("20.60", "OK")
                .downloadAndCheckNrdAnswer("OK")

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .setResUrl("https://smth.com")
                .saveRes()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .publish()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COMPLETED)
                .checkDocumentStatus(DocStatus.SEND)

                .checkLastProcessFinishedInRoutineOps(String.format("Проведение депозитного аукциона (Код БИ: %s)", stockCode));
    }

    @Test
    @Order(28)
    @Link(name = "SPB-T285", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T285")
    @DisplayName("SPB-T285. ВЭБ РФ - аукцион: заявки поданы, сделки есть ________(включая НРД: OK/DEAL и т.д.) ____________DVEB10S009")
    public void t285() {
        Credentials.setEnv("test");
        String paoSber = "ПАО \"Сбербанк\"";
        String stockCode = "DVEB10S010";

        LocalDateTime requestFrom = LocalDateTime.now().plusMinutes(2);
        LocalDateTime requestTo = LocalDateTime.now().plusMinutes(6);
        LocalDateTime requestBidPlusTo = LocalDateTime.now().plusMinutes(15);
        LocalDateTime raiseBidTo = LocalDateTime.now().plusMinutes(9);
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF)
                .createAuc(new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.AUCTION))
                .fillAucNoCur("B0", "900", "today", "10", "Открытый",
                        requestFrom.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        requestTo.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        requestBidPlusTo.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        "DVEB10SX6X7X8", "XMXX - VBRF", "today", "0.01")
                .setRateIncreaseTime(
                        requestTo.format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        requestTo.plusMinutes(3).format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue()))
                ).setReturnDate(LocalDateTime.now().plusDays(10).format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT1.getValue())));

        new AuctionInitSteps()
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED)
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .refreshBroker()
                .waitUntil(requestFrom.plusSeconds(10));

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("30");
        list2.add("70000");
        list.add(list2);

        new AuctionInitSteps()
                .loginQuikCreateApl(QUIK_SBER, "Депозтиты \"ВЕБ.РФ\"", stockCode, list)
                .waitUntil(requestTo)
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "31")
                .checkBid(31.0)
                .raiseBid("32")
                .checkBid(32.0)
                .closeDesktop();

        List<List<String>> requests1 = List.of(List.of("32.00", "70"));
        List<List<String>> requests2 = List.of(List.of("32.00", "70000"));

        new AuctionInitSteps()
                .waitUntil(raiseBidTo.plusMinutes(1))
                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(requests1, paoSber)
                .hasSendApplicationsButton()
                .sendApplications()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .loginQuikCreateApl(QUIK_VEB, stockCode, requests2)
                .closeDesktop()
                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()
                .hasSendTradesButton()
                .sendTrades()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(2, Pattern.compile("(SPCEX_DOC_DS_594_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) +
                        "_\\d{5}.xlsx|VEBRF_\\d{4}_VTD06_10_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.FILE_DATE_FORMAT.getValue())) +
                        ".xml)"))
                .checkDownloadRegistrySentNrdButtonExist();
    }

    @Test
    @Order(13)
    @Link(name = "SPB-T318", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T318")
    @DisplayName("+ВЭБ РФ - Проверка изменения статуса НРД по инструменту DVEB10S009 ( ВЭБ РФ) ///13:40 - 13:50")
    public void t318() {
        new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.VEBRF)
                .checkNrd("DVEB10S009");
    }

    @Test
    @Order(23)
    @Link(name = "SPB-T320", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T320")
    @DisplayName("SPB-T320. КФ ЛО - ТОРГИ: заявки поданы, сделки есть_____DL1000S0007_///17:33 - 17:54")
    public void SPB_T320() {
        Credentials.setEnv("test");
        String stockCode = "DL1000S0007";
        String bankName = "ПАО \"Сбербанк\"";
        String bankCode = "574";

        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount(stockCode) + 1;

        LocalTime inputAppFrom = LocalTime.now().plusMinutes(3);
        LocalTime inputAppTo = LocalTime.now().plusMinutes(5);
        LocalTime rateIncreaseTo = inputAppTo.plusMinutes(3);
        LocalTime betTo = rateIncreaseTo.plusMinutes(15);

        AuctionInitSteps steps = new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFLO)
                .createTrade(DepositAuctionNTradesPage.TradeType.AUCTION, new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.TRADE))

                .fillTrade("B0", "today", "7", "Открытый", "Фиксированная ставка",
                        inputAppFrom.format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        inputAppTo.format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        betTo.format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        "DL1000S0X6X7X8", "XMNS - KFLO", "today", "RUB", "По ставке" +
                                " Уполномоченного банка", "5", "900000")
                .setRateIncreaseTime(inputAppTo.format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())),
                        rateIncreaseTo.format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())))

                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED);

        steps
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .refreshBroker()
                .waitUntil(LocalDateTime.now().withHour(inputAppFrom.getHour()).withMinute(inputAppFrom.getMinute()));

        List<List<String>> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list2.add("15");
        list2.add("50000");
        list.add(list2);

        steps
                .loginQuikCreateApl(QUIK_SBER, "Депозитные торги/аукцион \"КФ ЛО\"", stockCode, list)
                .waitUntil(LocalDateTime.now().withHour(inputAppTo.getHour()).withMinute(inputAppTo.getMinute()))
                .raiseBidWithOpenWindow(RequestPage.getInstance().getRequestNumbers().get(0), "16")
                .closeDesktop();

        List<String> change = new ArrayList<>();
        change.add("16");
        change.add("50000000");
        list.remove(0);
        list.add(0, change);

        List<List<String>> webList = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        list3.add("16");
        list3.add("50000");
        webList.add(list3);

        steps
                .waitUntil(LocalDateTime.now().withHour(rateIncreaseTo.plusMinutes(1).getHour()).withMinute(rateIncreaseTo.plusMinutes(1).getMinute()))

                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkApl(webList, bankName)
                .hasSendApplicationsButton()
                .sendApplicationsAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_"+bankCode+"_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .loginQuikCreateApl(QUIK_FINANCE_LO, "Депозитные торги/аукцион \"КФ ЛО\"", stockCode, list)
                .closeDesktop()

                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealApl(webList, bankName)
                .getChangedToUpdate()
                .hasSendTradesButton()
                .sendTradesAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_" + bankCode + "_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()

                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_" + bankCode + "_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkRealVolumeAccess()
                .checkAverageRateAccess()

                .fillRealVolumeAndAverageRates("50000000", "16")
                .sendAuctionResults();

        WaitingUtils.sleep(20);
        Assertions.assertThat(steps.getAucCount(stockCode)).isEqualTo(count);
    }

    @Test
    @Order(22)
    @Link(name = "SPB-T319", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T319")
    @DisplayName("SPB-T319. КФ ЛО - ТОРГИ: заявки не поданы, сделок нет_____________DL1000K0038 ///17:15 - 17:30")
    public void SPB_T319() {
        Credentials.setEnv("test");
        String stockCode = "DL1000K0038";
        String bankCode = "574";

        int count = new AuctionInitSteps()
                .openSiteGoToInitAuc()
                .getAucCount(stockCode) + 1;



        AuctionInitSteps steps = new AuctionInitSteps()
                .openSection(DepositAuctionNTradesPage.UiAuctionsNTradeSection.KFLO)
                .createTrade(DepositAuctionNTradesPage.TradeType.AUCTION, new File("src/test/resources/empty_pdf.pdf"))
                .openTradeOrAuc(new AuctionInitDbHelper().getLastName("pdf", AuctionInitDbHelper.Type.TRADE))

                .fillTrade("B0", "today", "38", "Открытый", "Фиксированная ставка",
                        currTimePlusMin(4, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(6, PROCESSING_APP_TIME_FORMAT),
                        currTimePlusMin(9, PROCESSING_APP_TIME_FORMAT),
                        "DL1000K0X6X7X8", "XMNS - KFLO", "today", "RUB", "По ставке" +
                                " Уполномоченного банка", "5", "900");
        LocalDateTime time = LocalDateTime.now().plusMinutes(8);
        steps
                .saveParams()
                .checkGeneralStatus(OpenedAucOrTradeParamsPage.GeneralStatus.IN_WORK)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .exportEditDeclineExists()
                .export()
                .checkDocumentStatus(DocStatus.EXPORTED);

        steps
                .send()
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.WAITING)
                .checkDocumentStatus(DocStatus.FINISHED)
                .checkAutofill(stockCode)
                .waitUntil(time);

        steps

                .openSection(OpenAucOrTradeHeaderPage.Section.APPLICATIONS, new OpenedAucOrTradeApplicationsPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_APPLICATIONS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoApplications()
                .hasSendApplicationsButton()
                .sendApplicationsAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.SEND)
                .checkApplicationDocGen(4, Pattern.compile("SPCEX_DOC_DS_"+bankCode+"_IORDERS_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .openSection(OpenAucOrTradeHeaderPage.Section.TRADES, new OpenedAucOrTradeTradesPage())
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.NEW)
                .hasNoTrades()
                .getTrades()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.COLLECTING_TRADES)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .hasNoTrades()
                .getChangedToUpdate()
                .hasSendTradesButton()
                .sendTradesAllFormats()

                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.SEND)
                .checkTradesDocGen(4, Pattern.compile("SPCEX_DOC_DS_" + bankCode + "_ITRADES_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.(pdf|docx|xml|dbf)"))

                .openSection(OpenAucOrTradeHeaderPage.Section.RESULTS, new OpenedAurOrTradeResultsPage())
                .checkDocumentStatus(DocStatus.NEW)
                .checkUploadFileExists()

                .uploadFileResult(new File("src/test/resources/empty_pdf.pdf"))
                .checkResultDocGen(1, Pattern.compile("SPCEX_DOC_DS_" + bankCode + "_RESULT_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "_\\d{5}.pdf"))
                .checkGeneralStatus(OpenAucOrTradeHeaderPage.GeneralStatus.RESULTS)
                .checkDocumentStatus(DocStatus.IN_WORK)
                .checkInvalidAucArea()
                .fillInvalidAucReason("тест")
                .decline()
                .checkDocumentStatus(DocStatus.SEND)
                .checkGeneralStatus(DECLINED);

        WaitingUtils.sleep(20);
        Assertions.assertThat(steps.getAucCount(stockCode)).isEqualTo(count);
    }
}
