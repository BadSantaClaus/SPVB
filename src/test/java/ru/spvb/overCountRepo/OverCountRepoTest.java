package ru.spvb.overCountRepo;

import com.codeborne.selenide.Selenide;
import constants.Credentials;
import constants.Extension;
import constants.Initiator;
import constants.ProcessName;
import db.initiators.auctions.AuctionInitDbHelper;
import db.initiators.overCountRepo.OverCountRepoDbHelper;
import elements.columns.OverCountRepoColumn;
import io.qameta.allure.Epic;
import io.qameta.allure.Link;
import model.dbo.OverCountRepo;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import page.web.initiators.InitiatorsPage;
import page.web.initiators.auction.FilterInitiators;
import page.web.initiators.overCountRepo.OverCountRepoAppPage;
import ru.spvb.auction.common.BaseTest;
import ru.spvb.steps.FilterSteps;
import ru.spvb.steps.overCountRepo.OverCountRepoSteps;
import utils.PfxUtils;
import utils.WaitingUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.DateFormat.*;
import static constants.DocStatus.RESULTS;
import static constants.DocStatus.*;
import static constants.FilePath.*;
import static constants.XmlAttribute.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static page.quik.CreateAppPage.FieldNames.*;
import static page.web.initiators.overCountRepo.OverCountRepoAppPage.Tabs.*;
import static utils.XmlUtils.getTimeFromRepoLo;

@Order(3)
@Epic("Внебиржевое РЕПО")
public class OverCountRepoTest extends BaseTest {

    @Test
    @Link(name = "SPB-T299", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T299")
    @DisplayName("SPB-T299. Фильтры поиска: вкладка \"Внебиржевое РЕПО\"")
    public void t_299() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));
        AuctionInitDbHelper dbHelper = new AuctionInitDbHelper();
        SoftAssertions softly = new SoftAssertions();


        List<String> repoWithMask = dbHelper.getAuctionsWithNameTemplate("REPO_LO_AUCTION_%%_%%")
                .stream().map(a -> a.name).toList();

        String exchangeInstrument = "SPRV007RS1";
        LocalDateTime from = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(1);
        LocalDateTime to = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        List<String> statuses = List.of("Новый", "Отклонен", "Завершен");

        OverCountRepoSteps overCountRepoPage = new OverCountRepoSteps();
        FilterSteps filterSteps = new FilterSteps();
        overCountRepoPage.openPage();
        List<String> defaultNames = List.of();
        for (int i = 0; i < 5; i++) {
            defaultNames = new FilterInitiators().getColumnStrings(OverCountRepoColumn.NAME_DOC);
            if(defaultNames.isEmpty()){
                Selenide.sleep(5000);
            }else{
                break;
            }
        }

        filterSteps
                .setFilter(InitiatorsPage.FilterColumns.NAME_DOC, List.of(repoWithMask.get(0)))
                .checkColumnHasSameStrings(OverCountRepoColumn.NAME_DOC, List.of(repoWithMask.get(0)), softly);
        overCountRepoPage.clearFilter();
        filterSteps
                .setFilter(InitiatorsPage.FilterColumns.EXCHANGE_INSTRUMENT, List.of(exchangeInstrument))
                .setFilter(InitiatorsPage.FilterColumns.DATE, List.of(
                        from.format(ofPattern(XML_DATE_FORMAT.getValue())),
                        to.format(ofPattern(XML_DATE_FORMAT.getValue())))
                )
                .checkColumnHasSameStrings(OverCountRepoColumn.EXCHANGE_INSTRUMENT, List.of(exchangeInstrument), softly)
                .checkFilteredDatesBetween(OverCountRepoColumn.DATE_RECEIVE, from, to, WEB_DATE_TIME_FORMAT, softly)
                .setFilter(InitiatorsPage.FilterColumns.STATUS, statuses)
                .checkColumnHasSameStrings(OverCountRepoColumn.EXCHANGE_INSTRUMENT, List.of(exchangeInstrument), softly)
                .checkFilteredDatesBetween(OverCountRepoColumn.DATE_RECEIVE, from, to, WEB_DATE_TIME_FORMAT, softly)
                .checkFilteredColumnContains(OverCountRepoColumn.STATUS, statuses, softly);
        overCountRepoPage.clearFilter();
        filterSteps.checkColumnHasSameStringsOnPage(OverCountRepoColumn.NAME_DOC, defaultNames, softly);
        softly.assertAll();
    }

    @Test
    @Link(name = "SPB-T300", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T300")
    @DisplayName("SPB-T300. КФ ЛО - заявки НЕ ПОДАНЫ, сделок нет __________LORA002RS0")
    public void t_300() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";
        String biCode = "LORA002RS0";
        PfxUtils.createRepoLoFile(aucName, Map.of());
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());
        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.LO, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.LO, biCode);
        steps.openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                )).exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .refreshBroker()
                .waitUntil(bookingFinish.plusMinutes(1).toLocalTime())
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableEmpty()
                .clickSend()
                .checkAucStatus(RESULTS)
                .checkDocStatus(SEND)
                .checkTabBlocked(COUNTERCLAIMS_REGISTRY)
                .checkTabBlocked(TRANSACTIONS_REGISTRY)
                .checkResultsInSftp()
                .openTab(OverCountRepoAppPage.Tabs.RESULTS)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .clickReject()
                .checkAucStatus(DECLINED)
                .checkDocStatus(SEND)
                .checkProcessFinishedInRoutineOps(String.format(ProcessName.OVER_COUNT_REPO_TEMPLATE.getValue(), biCode));

    }

    @Test
    @Link(name = "SPB-T301", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T301")
    @DisplayName("SPB-T301. КФ ЛО - заявки поданы, ВСЕ ОТКЛОНЕНЫ______________LORA007RS0")
    public void t301() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";


        String biCode = "LORA007RS0";
        PfxUtils.createRepoLoFile(aucName, Map.of(
                FINSTR, "LORA007RS0",
                PERIOD, "7"
        ));

        Map<String, String> appData = Map.of(
                PARTNER.getValue(), "574",
                BUY_SELL.getValue(), "true",
                SUM_REPO.getValue(), String.valueOf(1000 * 1000),
                LOTS.getValue(), "1",
                CODE_CALC.getValue(), "B0",
                RATE_REPO.getValue(), "16",
                CODE_CLIENT.getValue(), "0045CAT00001"
        );

        LocalDateTime bookingStart = getTimeFromRepoLo(aucName, BOOKING_TIME_START.getValue());
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());

        String repoLoAccept = "REPO_LO_ACCEPT.xml";

        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.LO, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.LO, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                ))
                .exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .refreshBroker()
                .closeBrowser()
                .openQuik(QUIK_AB_RUSSIA)
                .loadLimFileQuik(RKFL_LIM)
                .waitUntil(bookingStart.toLocalTime())
                .createAppQuik(biCode, appData)
                .closeQuik()
                .waitUntil(bookingFinish.toLocalTime().plusMinutes(1))
                .openPage().filterByName(aucName).openAuc(aucName)
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableRowExists(Map.of(
                        RATE_REPO.getValue(), "16.00",
                        "Сумма РЕПО в валюте расчетов", "1 000 000.00"
                ))
                .clickSend()
                .checkAucStatus(COUNTER_REQUESTS)
                .checkDocStatus(SEND)
                .checkResultsInSftp()
                .generateAndUploadSpceFile(repoLoAccept, "574", Map.of(appData.get(SUM_REPO.getValue()), "R"),
                        Map.of())
                .openTab(COUNTERCLAIMS_REGISTRY)
                .uploadFileInAuc(TEMP_FILES.getValue() + repoLoAccept)
                .checkAucDocNames(List.of(
                        String.format("REPO_LO_ACCEPT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT1.getValue()))),
                        "Скачать tri-файл"
                ))
                .checkTableRowExists(Map.of(
                        RATE_REPO.getValue(), "16.00",
                        "Сумма РЕПО в валюте расчетов", "1 000 000.00",
                        "Статус заявки", "R"
                ))
                .openTab(TRANSACTIONS_REGISTRY)
                .clickReceive()
                .checkAucStatus(COLLECT_DEALS)
                .checkDocStatus(IN_WORK)
                .checkButtonVisible("Обновить")
                .clickSend()
                .checkAucStatus(RESULTS)
                .checkDocStatus(SEND)
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_574_REPOTRADES_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue()))),
                        "Скачать реестр отправленных сделок в НРД"))
                .checkTradeRegistry(List.of())
                .openTab(OverCountRepoAppPage.Tabs.RESULTS)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_574_RESULT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())))
                ))
                .clickReject()
                .checkAucStatus(DECLINED)
                .checkDocStatus(SEND)
                .checkProcessFinishedInRoutineOps(String.format(ProcessName.OVER_COUNT_REPO_TEMPLATE.getValue(), biCode));
    }

    @Test
    @Link(name = "SPB-T302", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T303")
    @DisplayName("SPB-T302. КФ ЛО - заявки поданы, полное + частичное исполнение (включая НРД: OK/DEAL и т.д.)___________LORA014RS0")
    public void t302() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";


        String biCode = "LORA014RS0";
        PfxUtils.createRepoLoFile(aucName, Map.of(
                FINSTR, biCode,
                PERIOD, "14",
                REPAYM_DATE, LocalDate.now().plusDays(14).format(ofPattern(XML_DATE_FORMAT.getValue()))
        ));
        LocalDateTime bookingStart = getTimeFromRepoLo(aucName, BOOKING_TIME_START.getValue());
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());

        Map<String, String> appData1 = Map.of(
                PARTNER.getValue(), "574",
                BUY_SELL.getValue(), "true",
                SUM_REPO.getValue(), String.valueOf(2 * 1000 * 1000),
                LOTS.getValue(), "2",
                CODE_CALC.getValue(), "B0",
                RATE_REPO.getValue(), "16",
                CODE_CLIENT.getValue(), "0045CAT00001"
        );

        Map<String, String> appData2 = new HashMap<>(appData1);
        appData2.putAll(Map.of(
                SUM_REPO.getValue(), String.valueOf(6 * 1000 * 1000),
                LOTS.getValue(), "6",
                RATE_REPO.getValue(), "17"
        ));
        String newApp2Sum = String.valueOf(4 * 1000 * 1000);

        Map<String, String> appData3 = new HashMap<>(appData1);
        appData3.putAll(Map.of(
                SUM_REPO.getValue(), String.valueOf(9 * 1000 * 1000),
                LOTS.getValue(), "9",
                RATE_REPO.getValue(), "18"
        ));

        String repoLoAccept = "REPO_LO_ACCEPT.xml";


        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.LO, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.LO, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                ))
                .exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .checkAucStatus(WAIT)
                .checkDocStatus(FINISHED)
                .checkReportsSftp(aucName)
                .refreshBroker()
                .closeBrowser()
                .openQuik(QUIK_AB_RUSSIA)
                .loadLimFileQuik(RKFL_LIM)
                .waitUntil(bookingStart.toLocalTime())
                .createAppQuik(biCode, appData1)
                .createAppQuik(biCode, appData2)
                .createAppQuik(biCode, appData3)
                .closeQuik()
                .waitUntil(bookingFinish.toLocalTime().plusMinutes(1))
                .openPage().filterByName(aucName).openAuc(aucName)
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableRowAppReg(appData1)
                .checkTableRowAppReg(appData2)
                .checkTableRowAppReg(appData3)
                .clickSend()
                .checkAucStatus(COUNTER_REQUESTS)
                .checkDocStatus(SEND)
                .checkResultsInSftp()
                .generateAndUploadSpceFile(repoLoAccept, "574", Map.of(
                        appData1.get(SUM_REPO.getValue()), "R",
                        appData2.get(SUM_REPO.getValue()), "P",
                        appData3.get(SUM_REPO.getValue()), "A"
                ), Map.of(appData2.get(SUM_REPO.getValue()), newApp2Sum))
                .openTab(COUNTERCLAIMS_REGISTRY)
                .uploadFileInAuc(TEMP_FILES.getValue() + repoLoAccept)
                .checkAucDocNames(List.of(
                        String.format("REPO_LO_ACCEPT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT1.getValue()))),
                        "Скачать tri-файл"
                ))
                .checkTableRowCounterClaimsReg(appData1, "R")
                .checkTableRowCounterClaimsReg(appData2, "P")
                .checkTableRowCounterClaimsReg(appData3, "A")
                .downloadExtraPlacementLoFile()
                .prepareLoTroTrrFile("LO")
                .processDynamicTransaction(QUIK_FINANCE_LO)
                .openTab(TRANSACTIONS_REGISTRY)
                .clickReceive()
                .checkAucStatus(COLLECT_DEALS)
                .checkDocStatus(IN_WORK)
                .checkTableTransactionsReg(appData2 = new HashMap<>(appData2) {{
                    put(SUM_REPO.getValue(), String.valueOf(4 * 1000 * 1000));
                }})
                .checkTableTransactionsReg(appData3)
                .checkButtonVisible("Обновить")
                .clickSend()
                .checkAucStatus(RESULTS)
                .checkDocStatus(SEND)
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_574_REPOTRADES_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue()))),
                        "Скачать реестр отправленных сделок в НРД"))
                .checkTradeRegistry(List.of(
                        appData2.get(SUM_REPO.getValue()),
                        appData3.get(SUM_REPO.getValue())
                ))
                .openTab(NRD)
                .clickGetStatusFromNrd()
                .checkAucStatus(RESULTS)
                .checkDocStatus(IN_WORK)
                .checkTableNrd(appData2, "NEW")
                .checkTableNrd(appData3, "NEW")
                .checkNrdAnswer("NEW")
                .waitUntil(LocalTime.now().plusMinutes(5))
                .clickGetStatusFromNrd()
                .checkTableNrd(appData2, "ERR")
                .checkTableNrd(appData3, "ERR")
                .checkNrdAnswer("ERR")
                .waitUntil(LocalTime.now().plusMinutes(5))
                .clickGetStatusFromNrd()
                .checkTableNrd(appData2, "OK")
                .checkTableNrd(appData3, "OK")
                .checkNrdAnswer("OK")
                .openTab(OverCountRepoAppPage.Tabs.RESULTS)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_574_RESULT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())))
                ))
                .checkAucStatus(RESULTS)
                .checkDocStatus(IN_WORK)
                .fillResultTabFields("13", "17.5")
                .checkAucStatus(FINISHED)
                .checkDocStatus(SEND)
                .checkProcessFinishedInRoutineOps(biCode);
    }

    @Test
    @Link(name = "SPB-T303", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T302")
    @DisplayName("SPB-T303. Автоматическое отклонение РЕПО КФ ЛО, если в XML прошлая дата __________LORA001RS0")
    public void t303() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";

        String biCode = "LORA001RS0";
        PfxUtils.createRepoLoFile(aucName, Map.of(
                FINSTR, biCode,
                TOTAL_SUM, "500",
                PERIOD, "1",
                PAYING_DATE, "2024-03-28",
                REPAYM_DATE, "2024-04-05",
                BOOKING_TIME_START, "18:10:00",
                BOOKING_TIME_FINISH, "18:13:00"
        ));

        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.LO, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastNameAfterTime(Initiator.LO, LocalDateTime.now().minusMinutes(5));
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(DECLINED)
                .checkDocStatus(ERROR)
                .checkProcessNotExistInRoutineOps(biCode);
    }

    @Test
    @Link(name = "SPB-T304", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T304")
    @DisplayName("SPB-T304. Автоматическое отклонение РЕПО КФ ЛО по завершении дня (До экспорта в ТС, статус - Новый)__________LORA005RS0")
    public void t304() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";


        String biCode = "LORA005RS0";
        PfxUtils.createRepoLoFile(aucName, Map.of(
                FINSTR, biCode,
                PERIOD, "5",
                RATE_TYPE, "FIX",
                PAYING_DATE, LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue())),
                REPAYM_DATE, LocalDate.now().plusDays(5).format(ofPattern(XML_DATE_FORMAT.getValue())),
                MIN_ORD_RATE, "10"
        ));

        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.LO, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.LO, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK);
    }

    @Test
    @Link(name = "SPB-T305", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T305")
    @DisplayName("SPB-T305. Автоматическое отклонение РЕПО КФ ЛО по завершении дня (после экспорта в ТС, статус - Сбор сделок)____________________LORA004RS0")
    public void t305() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";

        String biCode = "LORA028RS0";
        PfxUtils.createRepoLoFile(aucName, new HashMap<>() {{
            put(FINSTR, biCode);
            put(PERIOD, "28");
            put(PAYING_DATE, LocalDate.now().format(ofPattern(XML_DATE_FORMAT.getValue())));
            put(REPAYM_DATE, LocalDate.now().plusDays(28).format(ofPattern(XML_DATE_FORMAT.getValue())));
        }});

        Map<String, String> appData = Map.of(
                PARTNER.getValue(), "574",
                BUY_SELL.getValue(), "true",
                SUM_REPO.getValue(), String.valueOf(8 * 1000 * 1000),
                LOTS.getValue(), "8",
                CODE_CALC.getValue(), "B0",
                RATE_REPO.getValue(), "17",
                CODE_CLIENT.getValue(), "0045CAT00001"
        );

        LocalDateTime bookingStart = getTimeFromRepoLo(aucName, BOOKING_TIME_START.getValue());
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());

        String repoLoAccept = "REPO_LO_ACCEPT.xml";

        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.LO, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.LO, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                ))
                .exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .refreshBroker()
                .closeBrowser()
                .openQuik(QUIK_AB_RUSSIA)
                .loadLimFileQuik(RKFL_LIM)
                .waitUntil(bookingStart.toLocalTime())
                .createAppQuik(biCode, appData)
                .closeQuik()
                .waitUntil(bookingFinish.toLocalTime().plusMinutes(1))
                .openPage().filterByName(aucName).openAuc(aucName)
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableRowExists(Map.of(
                        RATE_REPO.getValue(), "17.00",
                        "Сумма РЕПО в валюте расчетов", "8 000 000.00"
                ))
                .clickSend()
                .checkAucStatus(COUNTER_REQUESTS)
                .checkDocStatus(SEND)
                .checkResultsInSftp()
                .generateAndUploadSpceFile(repoLoAccept, "574", Map.of(appData.get(SUM_REPO.getValue()), "A"),
                        Map.of())
                .openTab(COUNTERCLAIMS_REGISTRY)
                .uploadFileInAuc(TEMP_FILES.getValue() + repoLoAccept)
                .checkAucDocNames(List.of(
                        String.format("REPO_LO_ACCEPT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT1.getValue()))),
                        "Скачать tri-файл"
                ))
                .checkTableRowExists(Map.of(
                        RATE_REPO.getValue(), "17.00",
                        "Сумма РЕПО в валюте расчетов", "8 000 000.00",
                        "Статус заявки", "A"
                ))
                .checkAucStatus(COLLECT_DEALS)
                .checkDocStatus(SEND);
    }

    @Test
    @Link(name = "SPB-T306", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T306")
    @DisplayName("SPB-T306. КФ СПБ - заявки поданы, полное + частичное исполнение (включая вкладку НРД: статусы OK/DEAL и т.д.)__________________________SPRA007RS1")
    public void t306() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_SPB.xml";


        String biCode = "SPRA007RS1";
        PfxUtils.createRepoSpbFile(aucName, Map.of(
                FINSTR, biCode,
                PERIOD, "7",
                RATE_TYPE, "FIX",
                FIX_DISCOUNT, "",
                PAYING_DATE, LocalDate.now().plusDays(1).format(ofPattern(XML_DATE_FORMAT.getValue())),
                REPAYM_DATE, LocalDate.now().plusDays(7).format(ofPattern(XML_DATE_FORMAT.getValue())),
                BOOKING_TIME_FINISH, LocalTime.now().plusMinutes(8).format(ofPattern(XML_TIME_FORMAT.getValue()))
        ));
        LocalDateTime bookingStart = getTimeFromRepoLo(aucName, BOOKING_TIME_START.getValue());
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());

        Map<String, String> appData1 = Map.of(
                PARTNER.getValue(), "302",
                BUY_SELL.getValue(), "true",
                SUM_REPO.getValue(), String.valueOf(30 * 1000 * 1000),
                LOTS.getValue(), "30",
                CODE_CALC.getValue(), "B0",
                RATE_REPO.getValue(), "13",
                CODE_CLIENT.getValue(), "0045CAT00001"
        );

        Map<String, String> appData2 = new HashMap<>(appData1);
        appData2.putAll(Map.of(
                SUM_REPO.getValue(), String.valueOf(20 * 1000 * 1000),
                LOTS.getValue(), "20",
                RATE_REPO.getValue(), "15"
        ));
        String newApp2Sum = String.valueOf(5 * 1000 * 1000);

        Map<String, String> appData3 = new HashMap<>(appData1);
        appData3.putAll(Map.of(
                SUM_REPO.getValue(), String.valueOf(10 * 1000 * 1000),
                LOTS.getValue(), "10",
                RATE_REPO.getValue(), "17"
        ));

        String repoLoAccept = "REPO_LO_ACCEPT.xml";


        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.SPB, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.SPB, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                ))
                .exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .checkAucStatus(WAIT)
                .checkDocStatus(FINISHED)
                .checkReportsSftp(aucName)
                .refreshBroker()
                .closeBrowser()
                .openQuik(QUIK_AB_RUSSIA)
                .loadLimFileQuik(RKFL_LIM)
                .waitUntil(bookingStart.toLocalTime())
                .createAppQuik(biCode, appData1)
                .createAppQuik(biCode, appData2)
                .createAppQuik(biCode, appData3)
                .closeQuik()
                .waitUntil(bookingFinish.toLocalTime().plusMinutes(1))
                .openPage().filterByName(aucName).openAuc(aucName)
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableRowAppReg(appData1)
                .checkTableRowAppReg(appData2)
                .checkTableRowAppReg(appData3)
                .clickSend()
                .checkAucStatus(COUNTER_REQUESTS)
                .checkDocStatus(SEND)
                .checkResultsInSftp()
                .generateAndUploadSpceFile(repoLoAccept, "302", Map.of(
                        appData1.get(SUM_REPO.getValue()), "R",
                        appData2.get(SUM_REPO.getValue()), "P",
                        appData3.get(SUM_REPO.getValue()), "A"
                ), Map.of(appData2.get(SUM_REPO.getValue()), newApp2Sum))
                .openTab(COUNTERCLAIMS_REGISTRY)
                .uploadFileInAuc(TEMP_FILES.getValue() + repoLoAccept)
                .checkAucDocNames(List.of(
                        String.format("REPO_SP_ACCEPT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT1.getValue()))),
                        "Скачать tri-файл"
                ))
                .checkTableRowCounterClaimsReg(appData1, "R")
                .checkTableRowCounterClaimsReg(appData2, "P")
                .checkTableRowCounterClaimsReg(appData3, "A")
                .downloadExtraPlacementLoFile()
                .prepareLoTroTrrFile("SPB")
                .processDynamicTransaction(QUIK_FINANCE_SPB)
                .openTab(TRANSACTIONS_REGISTRY)
                .clickReceive()
                .checkAucStatus(COLLECT_DEALS)
                .checkDocStatus(IN_WORK)
                .checkTableTransactionsReg(appData2 = new HashMap<>(appData2) {{
                    put(SUM_REPO.getValue(), newApp2Sum);
                }})
                .checkTableTransactionsReg(appData3)
                .checkButtonVisible("Обновить")
                .clickSend()
                .checkAucStatus(RESULTS)
                .checkDocStatus(SEND)
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_302_REPOTRADES_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue()))),
                        "Скачать реестр отправленных сделок в НРД"))
                .checkTradeRegistry(List.of(
                        newApp2Sum,
                        appData3.get(SUM_REPO.getValue())
                ))
                .openTab(NRD)
                .clickGetStatusFromNrd()
                .checkAucStatus(RESULTS)
                .checkDocStatus(IN_WORK)
                .checkTableNrd(appData2, "NEW")
                .checkTableNrd(appData3, "NEW")
                .checkNrdAnswer("NEW")
                .waitUntil(LocalTime.now().plusMinutes(6))
                .clickGetStatusFromNrd()
                .checkTableNrd(appData2, "ERR")
                .checkTableNrd(appData3, "ERR")
                .checkNrdAnswer("ERR")
                .waitUntil(LocalTime.now().plusMinutes(6))
                .clickGetStatusFromNrd()
                .checkTableNrd(appData2, "OK")
                .checkTableNrd(appData3, "OK")
                .checkNrdAnswer("OK")
                .openTab(OverCountRepoAppPage.Tabs.RESULTS)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_302_RESULT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())))
                ))
                .checkAucStatus(RESULTS)
                .checkDocStatus(IN_WORK)
                .fillResultTabFields("13", "17.5")
                .checkAucStatus(FINISHED)
                .checkDocStatus(SEND)
                .checkProcessFinishedInRoutineOps(biCode);
    }

    @Test
    @Link(name = "SPB-T307", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T307")
    @DisplayName("SPB-T307. КФ СПБ - заявки поданы, все отклонены__________________________SPRV007RS1")
    public void t307() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_SPB.xml";


        String biCode = "SPRV007RS1";
        PfxUtils.createRepoSpbFile(aucName, Map.of(
                DOC_DATE, "2024-03-28",
                DOC_TIME, "09:05:00",
                DOC_NAME, "selection_info_20240328_090500.xml",
                SENDER_DOC_ID, "SI_2024032801",
                FINSTR, biCode,
                PERIOD, "7",
                TOTAL_SUM, "10000",
                MIN_ORD_RATE, "1.25",
                RATE_TYPE, "FLOATING",
                BENCHMARK, "RUONmDS"
        ));

        Map<String, String> appData = Map.of(
                PARTNER.getValue(), "302 [302]",
                BUY_SELL.getValue(), "true",
                SUM_REPO.getValue(), String.valueOf(6 * 1000 * 1000),
                LOTS.getValue(), "6",
                CODE_CALC.getValue(), "B0",
                RATE_REPO.getValue(), "16",
                CODE_CLIENT.getValue(), "0045CAT00001"
        );

        LocalDateTime bookingStart = getTimeFromRepoLo(aucName, BOOKING_TIME_START.getValue());
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());

        String repoLoAccept = "REPO_LO_ACCEPT.xml";

        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.SPB, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.SPB, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                ))
                .exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .refreshBroker()
                .closeBrowser()
                .openQuik(QUIK_AB_RUSSIA)
                .loadLimFileQuik(RKFL_LIM)
                .waitUntil(bookingStart.toLocalTime())
                .createAppQuik(biCode, appData)
                .closeQuik()
                .waitUntil(bookingFinish.toLocalTime().plusMinutes(1))
                .openPage().filterByName(aucName).openAuc(aucName)
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableRowExists(Map.of(
                        "Ставка РЕПО", "16.00",
                        "Сумма РЕПО в валюте расчетов", "6 000 000.00"
                ))
                .clickSend()
                .checkAucStatus(COUNTER_REQUESTS)
                .checkDocStatus(SEND)
                .checkResultsInSftp()
                .generateAndUploadSpceFile(repoLoAccept, "302", Map.of(appData.get(SUM_REPO.getValue()), "R"),
                        Map.of())
                .openTab(COUNTERCLAIMS_REGISTRY)
                .uploadFileInAuc(TEMP_FILES.getValue() + repoLoAccept)
                .checkAucDocNames(List.of(
                        String.format("REPO_SP_ACCEPT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT1.getValue()))),
                        "Скачать tri-файл"
                ))
                .checkTableRowExists(Map.of(
                        "Ставка РЕПО", "16.00",
                        "Сумма РЕПО в валюте расчетов", "6 000 000.00",
                        "Статус заявки", "R"
                ))
                .openTab(TRANSACTIONS_REGISTRY)
                .clickReceive()
                .checkAucStatus(COLLECT_DEALS)
                .checkDocStatus(IN_WORK)
                .checkButtonVisible("Обновить")
                .clickSend()
                .checkAucStatus(RESULTS)
                .checkDocStatus(SEND)
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_302_REPOTRADES_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue()))),
                        "Скачать реестр отправленных сделок в НРД"))
                .checkTradeRegistry(List.of())
                .openTab(OverCountRepoAppPage.Tabs.RESULTS)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        String.format("SPCEX_DOC_DS_302_RESULT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())))
                ))
                .clickReject()
                .checkAucStatus(DECLINED)
                .checkDocStatus(SEND)
                .checkProcessFinishedInRoutineOps(String.format(ProcessName.OVER_COUNT_REPO_TEMPLATE.getValue(), biCode));
    }

    @Test
    @Link(name = "SPB-T308", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T308")
    @DisplayName("SPB-T308. Автоматическое отклонение РЕПО КФ СПБ по завершении дня (после экспорта в ТС, статус - Итоги)________________SPRV021RS1")
    public void t308() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";


        String biCode = "SPRV021RS1";
        PfxUtils.createRepoSpbFile(aucName, Map.of(
                FINSTR, biCode,
                TOTAL_SUM, "10000",
                PERIOD, "21",
                RATE_TYPE, "FLOATING",
                BENCHMARK, "RUONmDS",
                PAYING_DATE, LocalDate.now().plusDays(1).format(ofPattern(XML_DATE_FORMAT.getValue())),
                REPAYM_DATE, LocalDate.now().plusDays(14).format(ofPattern(XML_DATE_FORMAT.getValue())),
                MIN_ORD_RATE, "1.25"
        ));
        LocalDateTime bookingStart = getTimeFromRepoLo(aucName, BOOKING_TIME_START.getValue());
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());

        Map<String, String> appData1 = Map.of(
                PARTNER.getValue(), "302",
                BUY_SELL.getValue(), "true",
                SUM_REPO.getValue(), String.valueOf(25 * 1000 * 1000),
                LOTS.getValue(), "25",
                CODE_CALC.getValue(), "B0",
                RATE_REPO.getValue(), "5",
                CODE_CLIENT.getValue(), "0045CAT00001"
        );

        Map<String, String> appData2 = new HashMap<>(appData1);
        appData2.putAll(Map.of(
                SUM_REPO.getValue(), String.valueOf(70 * 1000 * 1000),
                LOTS.getValue(), "70",
                RATE_REPO.getValue(), "4"
        ));
        String newApp2Sum = String.valueOf(2 * 1000 * 1000);

        String repoLoAccept = "REPO_LO_ACCEPT.xml";


        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.SPB, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.SPB, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                ))
                .exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .checkAucStatus(WAIT)
                .checkDocStatus(FINISHED)
                .checkReportsSftp(aucName)
                .refreshBroker()
                .closeBrowser()
                .openQuik(QUIK_AB_RUSSIA)
                .loadLimFileQuik(RKFL_LIM)
                .waitUntil(bookingStart.toLocalTime())
                .createAppQuik(biCode, appData1)
                .createAppQuik(biCode, appData2)
                .closeQuik()
                .waitUntil(bookingFinish.toLocalTime().plusMinutes(1))
                .openPage().filterByName(aucName).openAuc(aucName)
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableRowAppReg(appData1)
                .checkTableRowAppReg(appData2)
                .clickSend()
                .checkAucStatus(COUNTER_REQUESTS)
                .checkDocStatus(SEND)
                .checkResultsInSftp()
                .generateAndUploadSpceFile(repoLoAccept, "302", Map.of(
                        appData1.get(SUM_REPO.getValue()), "A",
                        appData2.get(SUM_REPO.getValue()), "P"
                ), Map.of(appData2.get(SUM_REPO.getValue()), newApp2Sum))
                .openTab(COUNTERCLAIMS_REGISTRY)
                .uploadFileInAuc(TEMP_FILES.getValue() + repoLoAccept)
                .checkTableRowCounterClaimsReg(appData1, "A")
                .checkTableRowCounterClaimsReg(appData2 = new HashMap<>(appData2) {{
                    put(SUM_REPO.getValue(), newApp2Sum);
                }}, "P")
                .checkAucDocNames(List.of(
                        String.format("REPO_SP_ACCEPT_%s_", LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT1.getValue()))),
                        "Скачать tri-файл"
                ))
                .downloadExtraPlacementLoFile()
                .prepareLoTroTrrFile("SP")
                .processDynamicTransaction(QUIK_FINANCE_SPB)
                .openTab(TRANSACTIONS_REGISTRY)
                .clickReceive()
                .checkAucStatus(COLLECT_DEALS)
                .checkDocStatus(IN_WORK)
                .checkTableTransactionsReg(new HashMap<>(appData2) {{
                    put(SUM_REPO.getValue(), newApp2Sum);
                }}).checkTableTransactionsReg(appData1)
                .clickSend()
                .checkAucStatus(RESULTS)
                .checkDocStatus(SEND);
    }

    @Test
    @Link(name = "SPB-T309", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T309")
    @DisplayName("SPB-T309. КФ СПБ - заявки не поданы, сделок нет______________ SPRA002RS1")
    public void t_309() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_SPB.xml";
        String biCode = "SPRA002RS1";
        PfxUtils.createRepoSpbFile(aucName, Map.of(
                FIX_DISCOUNT, "",
                BOOKING_TIME_START, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                BOOKING_TIME_FINISH, LocalTime.now().plusMinutes(5).format(ofPattern(XML_TIME_FORMAT.getValue()))
        ));
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());
        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.SPB, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.SPB, biCode);
        steps.openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B1")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                )).exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .checkAucStatus(WAIT)
                .checkDocStatus(FINISHED)
                .refreshBroker()
                .waitUntil(bookingFinish.plusMinutes(1).toLocalTime())
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK)
                .checkTableEmpty()
                .clickSend()
                .checkAucStatus(RESULTS)
                .checkDocStatus(SEND)
                .checkTabBlocked(COUNTERCLAIMS_REGISTRY)
                .checkTabBlocked(TRANSACTIONS_REGISTRY)
                .checkResultsInSftp()
                .openTab(OverCountRepoAppPage.Tabs.RESULTS)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .clickReject()
                .checkAucStatus(DECLINED)
                .checkDocStatus(SEND)
                .checkProcessFinishedInRoutineOps(String.format(ProcessName.OVER_COUNT_REPO_TEMPLATE.getValue(), biCode));

    }

    @Test
    @Link(name = "SPB-T310", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T310")
    @DisplayName("SPB-T310. Автоматическое отклонение РЕПО КФ СПБ по завершении дня (после экспорта в ТС, статус - Сбор заявок)____________SPRV004RS1")
    public void t310() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";


        String biCode = "SPRV004RS1";
        PfxUtils.createRepoSpbFile(aucName, Map.of(
                FINSTR, biCode,
                TOTAL_SUM, "10000",
                PERIOD, "4",
                RATE_TYPE, "FLOATING",
                BENCHMARK, "RUONmDS",
                PAYING_DATE, LocalDate.now().plusDays(1).format(ofPattern(XML_DATE_FORMAT.getValue())),
                REPAYM_DATE, LocalDate.now().plusDays(5).format(ofPattern(XML_DATE_FORMAT.getValue())),
                MIN_ORD_RATE, "1.25"
        ));
        LocalDateTime bookingFinish = getTimeFromRepoLo(aucName, BOOKING_TIME_FINISH.getValue());

        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.SPB, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.SPB, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue())
                .checkAucDocNames(List.of(
                        aucName.substring(0, aucName.indexOf(".")) + Extension.DOCX.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.XML.getValue(),
                        aucName.substring(0, aucName.indexOf(".")) + Extension.PDF.getValue()
                ))
                .exportToTC()
                .checkAucStatus(IN_WORK)
                .checkDocStatus(EXPORTED)
                .clickSendUT()
                .checkAucStatus(WAIT)
                .checkDocStatus(FINISHED)
                .checkReportsSftp(aucName)
                .refreshBroker()
                .waitUntil(bookingFinish.toLocalTime().plusMinutes(1))
                .openTab(APP_REGISTRY)
                .checkAucStatus(COLLECT_APPS)
                .checkDocStatus(IN_WORK);
    }

    @Test
    @Link(name = "SPB-T312", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T312")
    @DisplayName("SPB-T312. Автоматическое отклонение РЕПО КФ СПБ по завершении дня (До экспорта в ТС, статус - В работе)______________ SPRA003RS1")
    public void t312() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        String aucName = "REPO_LO.xml";


        String biCode = "SPRA003RS1";
        PfxUtils.createRepoSpbFile(aucName, Map.of(
                FINSTR, biCode,
                TOTAL_SUM, "10000",
                PERIOD, "4",
                RATE_TYPE, "FIX",
                BENCHMARK, "",
                PAYING_DATE, LocalDate.now().plusDays(1).format(ofPattern(XML_DATE_FORMAT.getValue())),
                REPAYM_DATE, LocalDate.now().plusDays(5).format(ofPattern(XML_DATE_FORMAT.getValue())),
                MIN_ORD_RATE, "10"
        ));

        OverCountRepoSteps steps = new OverCountRepoSteps();

        steps.openPage()
                .startSelectAppsProcess(Initiator.SPB, biCode, TEMP_FILES.getValue() + aucName);
        aucName = dbHelper.getLastName(Initiator.SPB, biCode);
        steps
                .openPage().filterByName(aucName)
                .openAuc(aucName)
                .checkAucStatus(NEW)
                .checkDocStatus(IN_WORK)
                .insertPaymentTerm("B0")
                .checkAucStatus(IN_WORK)
                .checkDocStatus(IN_WORK)
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.DOCX.getValue())
                .uploadFileInAuc(FILE_TEMPLATES_DIR.getValue() + "test" + Extension.PDF.getValue());
    }

    @Test
    @Link(name = "SPB-T316", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T316")
    @DisplayName("SPB-T316. Проверка автоотклонения незавершенных аукционов РЕПО за прошлые дни")
    public void t316() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(1));

        OverCountRepoDbHelper dbHelper = new OverCountRepoDbHelper();
        List<OverCountRepo> statuses = dbHelper.getAppsBeforeTodayStatuses();
        SoftAssertions softly = new SoftAssertions();
        for (OverCountRepo s : statuses) {
            softly.assertThat(s.status)
                    .as("Проверить статус заявки " + s.name)
                    .containsAnyOf("REJECTED", "COMPLETED");
        }
        softly.assertAll();


        new OverCountRepoSteps()
                .openPage()
                .checkNrdResults("LORA014RS0")
                .checkNrdResults("SPRA007RS1");
    }

}
