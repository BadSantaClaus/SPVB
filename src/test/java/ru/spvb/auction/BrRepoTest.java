package ru.spvb.auction;

import constants.*;
import elements.columns.BankRussiaStepsColumn;
import io.qameta.allure.Epic;
import io.qameta.allure.Link;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.spvb.auction.common.BaseTest;
import ru.spvb.steps.auction.AuctionSteps;
import utils.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.DateFormat.*;
import static constants.Extension.XML;
import static constants.FileName.*;
import static constants.FilePath.*;
import static constants.ProcessName.*;
import static constants.StepName.*;
import static constants.XmlAttribute.*;
import static java.time.format.DateTimeFormatter.ofPattern;
import static page.quik.CreateAppPage.FieldNames.*;

@Epic("БР: РЕПО")
public class BrRepoTest extends BaseTest {

    private final String t250Name = "SPB-T250. Торги РЕПО с фикс. ставкой - заявки 1) отклонены 2) не поданы 3) удовлетворены.";
    private final String t313Name = "SPB-T313. Торги РЕПО с плав. ставкой - заявки 1) не поданы 2) отклонены 3) удовлетворены.";

    private final List<String> pfx13steps = List.of(
            "PFX13: Подготовка данных для формирования документа",
            "PFX13: Формирование и валидация документа",
            "PFX13: Подписание ЭЦП и шифрование документа",
            "PFX13: Отправка документа в БР"
    );

    private final Map<String, String> pfx13TagAttrs = Map.of(
            "TRADEDATE", LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue())),
            "EXCHANGE", "АО СПВБ",
            "FIRMNAME", "Банк России",
            "FIRMID", "9"
    );

    private final Map<String, String> quikAppData = Map.of(
            PARTNER.getValue(), "Центральный банк РФ",
            BUY_SELL.getValue(), "true",
            SUM_REPO.getValue(), String.valueOf(2 * 1000 * 1000),
            LOTS.getValue(), "0",
            CODE_CALC.getValue(), "B0",
            RATE_REPO.getValue(), "15",
            CODE_CLIENT.getValue(), "0472CAT00001"
    );

    String bidInstrument = "CBRF007RS0";
    String bidFloatInstrument = "CBRDFLTRS7";

    @Test
    @Order(1)
    @Link(name = "SPB-T248", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T248")
    @DisplayName("SPB-T248. Отключение инструментов РЕПО с Банком России. 9:15")
    public void t248_09_15() {
        Credentials.setEnv("test");
        String processName = "Отключение инструментов РЕПО с Банком России";
        new AuctionSteps()
                .waitTime(LocalTime.of(9, 15))
                .checkProcessStatusRoutineOps(processName, "Начало дня")
                .closeWebDriver()
                .loginQuik(QUIK_PSB)
                .openCreateTradesTableWindow()
                .checkCurrentTradeExist("Аукцион РЕПО", false)
                .checkCurrentTradeExist("Торги РЕПО", false);

    }

    @Test
    @Order(2)
    @Link(name = "SPB-T262", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T262")
    @DisplayName("SPB-T262. PFX09 первым файлом пришел INFOTYPE 2. 9:31")
    public void t262_09_31() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(9).withMinute(31));
        String fileNamePfx = SpvbUtils.generateFileNameIncreaseIndex(PFX09, XML, BR_IN);
        Map<XmlAttribute, String> data = new HashMap<>();
        data.put(INFOTYPE, "2");
        data.put(LIMIT, "3000000000.1");
        PfxUtils.createPfx09(fileNamePfx, data);
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx)
                .openProcess(PFX09_NOTIFICATION.getValue())
                .waitStepsFinished(List.of(String.format(PFX09_VALIDATION.getValue(),
                        fileNamePfx.substring(0, fileNamePfx.indexOf(".")))), 2 * 60)
                .waitStepWithColumn(
                        String.format(PFX09_EXPORT_TO_QUIK.getValue(),
                                fileNamePfx.substring(0, fileNamePfx.indexOf("."))),
                        BankRussiaStepsColumn.OPERATING_MODE,
                        "false")
                .closeWebDriver()
                .checkIncomingBalance(data.get(LIMIT), false);
    }

    @Test
    @Order(3)
    @Link(name = "SPB-T261", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T261")
    @DisplayName("SPB-T261. PFX09 - INFOTYPE 1, затем INFOTYPE 2, затем еще INFOTYPE 1, потом INFOTYPE 2. 9:34")
    public void t261_9_34() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(9).withMinute(34));
        Map<XmlAttribute, String> data = new HashMap<>();
        data.put(INFOTYPE, "1");
        data.put(LIMIT, "3000000000.1");
        AuctionSteps auctionSteps = new AuctionSteps()
                .waitTime(LocalTime.of(9, 34));

        String fileNamePfx = SpvbUtils.generateFileNameIncreaseIndex(PFX09, XML, BR_IN);


        PfxUtils.createPfx09(fileNamePfx, data);
        auctionSteps.sendPfx09AndCheckIncomingBalance(fileNamePfx, data);

        fileNamePfx = SpvbUtils.generateFileNameIncreaseIndex(PFX09, XML, BR_IN);
        data.put(INFOTYPE, "2");
        data.put(LIMIT, "4000000000.1");
        PfxUtils.createPfx09(fileNamePfx, data);
        auctionSteps.sendPfx09AndCheckIncomingBalance(fileNamePfx, data);

        fileNamePfx = SpvbUtils.generateFileNameIncreaseIndex(PFX09, XML, BR_IN);
        data.put(INFOTYPE, "1");
        data.put(LIMIT, "3000000000.1");
        PfxUtils.createPfx09(fileNamePfx, data);
        auctionSteps
                .sendXmlAndRename(fileNamePfx)
                .openProcess(PFX09_NOTIFICATION.getValue())
                .waitStepsFinished(List.of(String.format(PFX09_VALIDATION.getValue(),
                        fileNamePfx.substring(0, fileNamePfx.indexOf(".")))), 2 * 60)
                .waitStepWithColumn(
                        String.format(PFX09_EXPORT_TO_QUIK.getValue(),
                                fileNamePfx.substring(0, fileNamePfx.indexOf("."))),
                        BankRussiaStepsColumn.OPERATING_MODE,
                        "false");

        fileNamePfx = SpvbUtils.generateFileNameIncreaseIndex(PFX09, XML, BR_IN);
        data.put(INFOTYPE, "2");
        data.put(LIMIT, "5000000000.1");
        PfxUtils.createPfx09(fileNamePfx, data);
        auctionSteps.sendPfx09AndCheckIncomingBalance(fileNamePfx, data);
    }

    @Test
    @Order(4)
    @Link(name = "SPB-T245", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T245")
    @DisplayName("SPB-T245. Подготовка к проведению аукционов и торгов РЕПО (z-файлы). 10:00")
    public void t245_10_00() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(9).withMinute(55));
        new AuctionSteps()
                .sendZFiles(ZPFX11, 25)
                .sendZFiles(ZPFX13, 25)
                .loginQuik(QUIK_PSB)
                .loadLimFileQuik(BR_LIM);
    }

    @Test
    @Order(5)
    @Link(name = "SPB-T246", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T246")
    @DisplayName("SPB-T246. PFX13 (итоговый) до поступления PFX02. 10:04")
    public void t246_10_04() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(4));

        int startHH = 10;
        int startMin = 4;
        int startSec = 0;
        int endHH = 10;
        int endMin = 10;
        int endSec = 0;

        AuctionSteps auctionSteps = new AuctionSteps()
                .waitTime(LocalTime.of(10, 4))
                .openProcessWithPlannedStart(PFX13_SEND_FINAL_REPO_TO_BR,
                        LocalTime.of(startHH, startMin, startSec)
                ).waitStepsFinished(pfx13steps, 10 * 60);

        String namePfx13 = SpvbUtils.getFileNameCreatedBetween(PFX13, ".xml",
                BR_OUT_NO_SIGN,
                LocalDateTime.of(LocalDate.now(), LocalTime.of(startHH, startMin, startSec)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(endHH, endMin, endSec))
        );
        auctionSteps.checkPfx13Tag(namePfx13, false, pfx13TagAttrs, Map.of());
    }

    @Test
    @Order(6)
    @Link(name = "SPB-T236", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T236")
    @DisplayName("SPB-T236. CNY Аукцион РЕПО с фикс. ставкой: заявки не поданы. 10:08")
    public void t236_10_08() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(8));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(5).format(ofPattern(XML_TIME_FORMAT.getValue()))
        );
        PfxUtils.createPfx02(fileNamePfx02, pfxData);
        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .refreshBroker()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, END_TIME.getValue()).toLocalTime())
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 20 * 60)
                .checkListStepsOfProcess(List.of(IMPORT_UT_FROM_QUIK.getValue()));

    }

    @Test
    @Order(7)
    @Link(name = "SPB-T247", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T247")
    @DisplayName("SPB-T247. PFX13 (итоговый) после PFX02_1 до формирования сделок. 10:20")
    public void t247_10_20() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(20));

        int startHH = 10;
        int startMin = 18;
        int startSec = 0;
        int endHH = 10;
        int endMin = 25;
        int endSec = 0;


        AuctionSteps auctionSteps = new AuctionSteps()
                .openProcessWithPlannedStart(PFX13_SEND_FINAL_REPO_TO_BR,
                        LocalTime.of(startHH, startMin, startSec))
                .waitStepsFinished(pfx13steps, 10 * 60);

        String namePfx13 = SpvbUtils.getFileNameCreatedBetween(PFX13, ".xml",
                BR_OUT_NO_SIGN,
                LocalDateTime.of(LocalDate.now(), LocalTime.of(startHH, startMin, startSec)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(endHH, endMin + 1, endSec)));

        auctionSteps.checkPfx13Tag(namePfx13, true, pfx13TagAttrs, Map.of(
                "BOARDID", "RRYX",
                "BOARDNAME", "РЕПО с БР (аук/фикс) в юанях"
        ));
    }

    @Test
    @Order(8)
    @Link(name = "SPB-T237", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T237")
    @DisplayName("SPB-T237. CNY Аукцион РЕПО с фикс. ставкой: заявки поданы и отклонены. 10:25")
    public void t237_10_25() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(25));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(20).format(ofPattern(XML_TIME_FORMAT.getValue()))

        );

        String pfx12Name = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX12, XML, BR_IN);
        PfxUtils.createPfx02(fileNamePfx02, pfxData);
        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .refreshBroker()
                .closeWebDriver()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, START_TIME.getValue()).plusSeconds(30).toLocalTime())
                .loginQuik(QUIK_PSB)
                .createAppRepo("CBRA014YS0", quikAppData)
                .closeDesktop()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, END_TIME.getValue()).plusSeconds(30).toLocalTime())
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(pfx12Name, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        Double.valueOf(quikAppData.get(SUM_REPO.getValue())), "R"
                ))
                .sendXmlAndRename(pfx12Name)
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .checkAucChildProcessesFinished(String.format(AUCTION_REPO.getValue(), aucId), List.of(
                        PFX13_SEND_REPO_TRANSACTIONS_TO_BR.getValue(),
                        SEND_REPO_TO_NRD.getValue()
                ));
    }

    @Test
    @Order(9)
    @Link(name = "SPB-T242", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T242")
    @DisplayName("SPB-T242. CNY Аукцион РЕПО с фикс. ставкой: заявки поданы и удовлетворены. 10:46")
    public void t_242_10_46() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(10).withMinute(46));

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                SECOND_LEG_DATE, LocalDateTime.now().plusDays(7).format(ofPattern(XML_DATE_FORMAT.getValue())),
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(8).format(ofPattern(XML_TIME_FORMAT.getValue()))
        );

        Map<String, String> data1 = new HashMap<>(quikAppData);
        data1.put(SUM_REPO.getValue(), String.valueOf(1000 * 1000));
        Map<String, String> data2 = new HashMap<>(quikAppData);
        data2.put(SUM_REPO.getValue(), String.valueOf(2 * 1000 * 1000));
        Map<String, String> data3 = new HashMap<>(quikAppData);
        data3.put(SUM_REPO.getValue(), String.valueOf(3 * 1000 * 1000));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);
        String pfx12Name = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX12, XML, BR_IN);
        PfxUtils.createPfx02(fileNamePfx02, pfxData);

        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        String instrument = "CBRA007YS0";
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .closeWebDriver()
                .refreshBroker()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, START_TIME.getValue()).plusSeconds(30).toLocalTime())
                .loginQuik(QUIK_PSB)
                .createAppRepo(instrument, data1)
                .createAppRepo(instrument, data2)
                .createAppRepo(instrument, data3)
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(pfx12Name, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        Double.valueOf(data1.get(SUM_REPO.getValue())), "A",
                        Double.valueOf(data2.get(SUM_REPO.getValue())), "A",
                        Double.valueOf(data3.get(SUM_REPO.getValue())), "R"
                ))
                .sendXmlAndRename(pfx12Name)
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), PFX12_SEND_NOTIFICATION_TO_BR.getValue()), 5 * 60)
                .waitPfx12GetFromBrFinish(PFX12_GET_BR_REGISTRY_REPO.getValue(), pfx12Name.substring(0, pfx12Name.indexOf(".")))
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .downloadRepoCbrFile()
                .closeWebDriver()
                .prepareAuctionCbrTriFile(instrument)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .checkAucChildProcessesFinished(String.format(AUCTION_REPO.getValue(), aucId), List.of(
                        PFX13_SEND_REPO_TRANSACTIONS_TO_BR.getValue(),
                        SEND_REPO_TO_NRD.getValue()
                ));
    }

    @Test
    @Order(10)
    @Link(name = "SPB-T239", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T239")
    @DisplayName("SPB-T239. Плав. аукцион РЕПО - заявки не поданы. 11:08")
    public void t239_11_08() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(11).withMinute(8));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                CURRENCY, "RUB",
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(5).format(ofPattern(XML_TIME_FORMAT.getValue())),
                RATE_TYPE_AUCT, "RREFKEYR"
        );

        PfxUtils.createPfx02(fileNamePfx02, pfxData);
        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .refreshBroker()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, END_TIME.getValue()).toLocalTime())
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 20 * 60)
                .checkListStepsOfProcess(List.of(IMPORT_UT_FROM_QUIK.getValue()));

    }

    @Test
    @Order(11)
    @Link(name = "SPB-T240", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T240")
    @DisplayName("SPB-T240. Плав. аукцион РЕПО - заявки поданы и отклонены. 11:22")
    public void t240_11_22() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(11).withMinute(22));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                CURRENCY, "RUB",
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(7).format(ofPattern(XML_TIME_FORMAT.getValue())),
                RATE_TYPE_AUCT, "RREFKEYR"
        );

        String pfx12Name = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX12, XML, BR_IN);
        PfxUtils.createPfx02(fileNamePfx02, pfxData);
        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 10 * 60)
                .refreshBroker()
                .closeWebDriver()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, START_TIME.getValue()).plusMinutes(1).plusSeconds(30).toLocalTime())
                .loginQuik(QUIK_PSB)
                .createAppRepo("CBRV014RS0", quikAppData)
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(pfx12Name, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        Double.valueOf(quikAppData.get(SUM_REPO.getValue())), "R"
                ))
                .sendXmlAndRename(pfx12Name)
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .checkAucChildProcessesFinished(String.format(AUCTION_REPO.getValue(), aucId), List.of(
                        PFX13_SEND_REPO_TRANSACTIONS_TO_BR.getValue(),
                        SEND_REPO_TO_NRD.getValue()
                ));
    }

    @Test
    @Order(12)
    @Link(name = "SPB-T243", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T243")
    @DisplayName("SPB-T243. Плав. аукцион РЕПО - заявки поданы и удовлетворены. 11:40")
    public void t_243_11_40() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(11).withMinute(40));

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                CURRENCY, "RUB",
                SECOND_LEG_DATE, LocalDateTime.now().plusDays(7).format(ofPattern(XML_DATE_FORMAT.getValue())),
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(10).format(ofPattern(XML_TIME_FORMAT.getValue())),
                RATE_TYPE_AUCT, "RREFKEYR"
        );

        Map<String, String> data1 = new HashMap<>(quikAppData);
        data1.put(SUM_REPO.getValue(), String.valueOf(10 * 1000 * 1000));
        Map<String, String> data2 = new HashMap<>(quikAppData);
        data2.put(SUM_REPO.getValue(), String.valueOf(2 * 1000 * 1000));
        Map<String, String> data3 = new HashMap<>(quikAppData);
        data3.put(SUM_REPO.getValue(), String.valueOf(3 * 1000 * 1000));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);
        String pfx12Name = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX12, XML, BR_IN);
        PfxUtils.createPfx02(fileNamePfx02, pfxData);

        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        String instrument = "CBRV007RS0";
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .closeWebDriver()
                .refreshBroker()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, START_TIME.getValue()).plusMinutes(1).plusSeconds(30).toLocalTime())
                .loginQuik(QUIK_PSB)
                .createAppRepo(instrument, data1)
                .createAppRepo(instrument, data2)
                .createAppRepo(instrument, data3)
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(pfx12Name, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        Double.valueOf(data1.get(SUM_REPO.getValue())), "A",
                        Double.valueOf(data2.get(SUM_REPO.getValue())), "A",
                        Double.valueOf(data3.get(SUM_REPO.getValue())), "R"
                ))
                .sendXmlAndRename(pfx12Name)
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), PFX12_SEND_NOTIFICATION_TO_BR.getValue()), 5 * 60)
                .waitPfx12GetFromBrFinish(PFX12_GET_BR_REGISTRY_REPO.getValue(), pfx12Name.substring(0, pfx12Name.indexOf(".")))
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .downloadRepoCbrFile()
                .closeWebDriver()
                .prepareAuctionCbrTriFile(instrument)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .checkAucChildProcessesFinished(String.format(AUCTION_REPO.getValue(), aucId), List.of(
                        PFX13_SEND_REPO_TRANSACTIONS_TO_BR.getValue(),
                        SEND_REPO_TO_NRD.getValue()
                ));
    }

    @Test
    @Order(13)
    @Link(name = "SPB-T238", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T238")
    @DisplayName("SPB-T238. Фикс. аукцион РЕПО - заявки не поданы. 12:05")
    public void t238_12_05() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(12).withMinute(5));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                CURRENCY, "RUB",
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(5).format(ofPattern(XML_TIME_FORMAT.getValue())),
                SECOND_LEG_DATE, LocalDate.now().plusDays(21).format(ofPattern(XML_DATE_FORMAT.getValue())),
                RATE_TYPE_AUCT, "FIX"
        );

        PfxUtils.createPfx02(fileNamePfx02, pfxData);
        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .refreshBroker()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, END_TIME.getValue()).toLocalTime())
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 20 * 60)
                .checkListStepsOfProcess(List.of(IMPORT_UT_FROM_QUIK.getValue()));
    }

    @Test
    @Order(14)
    @Link(name = "SPB-T241", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T241")
    @DisplayName("SPB-T241. Фикс. аукцион РЕПО - заявки поданы и отклонены. 12:19")
    public void t241_12_19() {
        Credentials.setEnv("test");
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(12).withMinute(19));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                CURRENCY, "RUB",
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(8).format(ofPattern(XML_TIME_FORMAT.getValue())),
                RATE_TYPE_AUCT, "FIX"
        );

        String pfx12Name = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX12, XML, BR_IN);
        PfxUtils.createPfx02(fileNamePfx02, pfxData);
        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .refreshBroker()
                .closeWebDriver()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, START_TIME.getValue()).plusMinutes(1).plusSeconds(30).toLocalTime())
                .loginQuik(QUIK_PSB)
                .createAppRepo("CBRA014RS0", quikAppData)
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(pfx12Name, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        Double.valueOf(quikAppData.get(SUM_REPO.getValue())), "R"
                ))
                .sendXmlAndRename(pfx12Name)
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .checkAucChildProcessesFinished(String.format(AUCTION_REPO.getValue(), aucId), List.of(
                        PFX13_SEND_REPO_TRANSACTIONS_TO_BR.getValue(),
                        SEND_REPO_TO_NRD.getValue()
                ));
    }

    @Test
    @Order(15)
    @Link(name = "SPB-T244", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T244")
    @DisplayName("SPB-T244. Фикс. аукцион РЕПО - заявки поданы и удовлетворены. 12:40")
    public void t_244_12_40() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(12).withMinute(40));

        Map<XmlAttribute, String> pfxData = Map.of(
                AUCTION_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                CURRENCY, "RUB",
                SECOND_LEG_DATE, LocalDateTime.now().plusDays(7).format(ofPattern(XML_DATE_FORMAT.getValue())),
                START_TIME, LocalTime.now().plusMinutes(3).format(ofPattern(XML_TIME_FORMAT.getValue())),
                END_TIME, LocalTime.now().plusMinutes(8).format(ofPattern(XML_TIME_FORMAT.getValue())),
                RATE_TYPE_AUCT, "FIX"
        );

        Map<String, String> data1 = new HashMap<>(quikAppData);
        data1.put(SUM_REPO.getValue(), String.valueOf(1000 * 1000));
        Map<String, String> data2 = new HashMap<>(quikAppData);
        data2.put(SUM_REPO.getValue(), String.valueOf(20 * 1000 * 1000));
        Map<String, String> data3 = new HashMap<>(quikAppData);
        data3.put(SUM_REPO.getValue(), String.valueOf(3 * 1000 * 1000));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);
        String pfx12Name = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX12, XML, BR_IN);
        PfxUtils.createPfx02(fileNamePfx02, pfxData);

        int aucId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02));
        String instrument = "CBRA007RS0";
        new AuctionSteps()
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .closeWebDriver()
                .refreshBroker()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02, START_TIME.getValue()).toLocalTime())
                .loginQuik(QUIK_PSB)
                .createAppRepo(instrument, data1)
                .createAppRepo(instrument, data2)
                .createAppRepo(instrument, data3)
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(pfx12Name, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        Double.valueOf(data1.get(SUM_REPO.getValue())), "A",
                        Double.valueOf(data2.get(SUM_REPO.getValue())), "A",
                        Double.valueOf(data3.get(SUM_REPO.getValue())), "R"
                ))
                .sendXmlAndRename(pfx12Name)
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), PFX12_SEND_NOTIFICATION_TO_BR.getValue()), 5 * 60)
                .waitPfx12GetFromBrFinish(PFX12_GET_BR_REGISTRY_REPO.getValue(), pfx12Name.substring(0, pfx12Name.indexOf(".")))
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .downloadRepoCbrFile()
                .closeWebDriver()
                .prepareAuctionCbrTriFile(instrument)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .closeDesktop()
                .openProcess(String.format(AUCTION_REPO.getValue(), aucId))
                .waitStepsFinished(List.of(IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .checkAucChildProcessesFinished(String.format(AUCTION_REPO.getValue(), aucId), List.of(
                        PFX13_SEND_REPO_TRANSACTIONS_TO_BR.getValue(),
                        SEND_REPO_TO_NRD.getValue()
                ));
    }


    @Story(t250Name)
    @Order(16)
    @Test
    @Link(name = "SPB-T250", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t250Name + " 13:31")
    public void t_250_13_31() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(13).withMinute(31));

        Map<XmlAttribute, String> pfxData = Map.of(
                FIX_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                SECOND_LEG_DATE, LocalDateTime.now().plusDays(7).format(ofPattern(XML_DATE_FORMAT.getValue())),
                BID_START_TIME, LocalTime.now().withHour(13).withMinute(39).format(ofPattern(XML_TIME_FORMAT.getValue())),
                BID_END_TIME, LocalTime.now().withHour(15).withMinute(45).format(ofPattern(XML_TIME_FORMAT.getValue()))
        );

        Map<String, String> data1 = new HashMap<>(quikAppData);
        data1.put(SUM_REPO.getValue(), String.valueOf(7 * 1000 * 1000));

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);

        PfxUtils.createPfx02Bid(fileNamePfx02, pfxData);

        new AuctionSteps()
                .setGetAppsTime(Map.of(
                        LocalTime.of(LocalTime.now().getHour(), 30)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), false,
                        LocalTime.of(LocalTime.now().getHour(), 45)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), false,

                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 0)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), true,
                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 15)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), false,
                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 30)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), true,
                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 45)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), false,
                        LocalTime.of(LocalTime.now().plusHours(2).getHour(), 0)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), true
                ))
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 10 * 60)
                .closeWebDriver()
                .refreshBroker()
                .waitTime(XmlUtils.getTimeFromPfx02(fileNamePfx02, PFX02_BID, BID_START_TIME.getValue()).plusMinutes(1).plusSeconds(30).toLocalTime())
                .loginQuik(QUIK_PSB)
                .createAppRepo(bidInstrument, data1)
                .closeDesktop();

    }

    @Story(t250Name)
    @Test
    @Order(17)
    @Link(name = "SPB-T250", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t250Name + " 14:05")
    public void t_250_14_05() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(14).withMinute(5));

        String fileNamePfx02 = SpvbUtils.generateFileNameSaveIndex(PFX02, XML, BR_IN);
        String fileNamePfx12 = SpvbUtils.generateFileNameIncreaseIndex(PFX12, XML, BR_IN);

        SftpUtils.downloadFile(fileNamePfx02, BR_IN.getValue(), TEMP_FILES.getValue());
        int fixId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02, "//FIX/@FIX_ID"));
        String processName = String.format(PROCESSING_APP.getValue(),
                LocalTime.of(LocalTime.now().getHour(), 0)
                        .format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())));

        new AuctionSteps()
                .openChildProcess(String.format(BID_REPO_FIX.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(fileNamePfx12, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        7.0 * 1000 * 1000, "R"
                ))
                .sendXmlAndRename(fileNamePfx12)
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), PFX12_SEND_NOTIFICATION_TO_BR.getValue()), 5 * 60)
                .waitPfx12GetFromBrFinish(PFX12_GET_BR_REGISTRY_REPO.getValue(), fileNamePfx12.substring(0, fileNamePfx12.indexOf(".")))
                .openChildProcess(String.format(BID_REPO_FIX.getValue(), fixId), processName)
                .downloadRepoCbrFile()
                .closeWebDriver()
                .prepareAuctionCbrTriFile(bidInstrument)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .closeDesktop()
                .openChildProcess(String.format(BID_REPO_FIX.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .openChildProcess(processName, PFX13_SEND_REPO_TO_BR.getValue())
                .waitStepsFinished(List.of(), 5 * 60)
                .openChildProcess(processName, SEND_REPO_TO_NRD.getValue())
                .waitStepsFinished(List.of(), 5 * 60);
    }

    @Story(t250Name)
    @Test
    @Order(18)
    @Link(name = "SPB-T250", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t250Name + " 14:16")
    public void t_250_14_30() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(14).withMinute(31));

        String fileNamePfx02 = SpvbUtils.generateFileNameSaveIndex(PFX02, XML, BR_IN);

        SftpUtils.downloadFile(fileNamePfx02, BR_IN.getValue(), TEMP_FILES.getValue());
        int fixId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02, "//FIX/@FIX_ID"));
        String processName = String.format(PROCESSING_APP.getValue(),
                LocalTime.of(LocalTime.now().getHour(), 30)
                        .format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())));

        new AuctionSteps()
                .openChildProcess(String.format(BID_REPO_FIX.getValue(), fixId), processName)
                .checkListStepsOfProcess(List.of(IMPORT_UT_FROM_QUIK.getValue()))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60);

    }

    @Story(t250Name)
    @Test
    @Order(19)
    @Link(name = "SPB-T250", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t250Name + " 14:46")
    public void t_250_14_46() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(14).withMinute(46));

        Map<String, String> data1 = new HashMap<>(quikAppData);
        data1.put(SUM_REPO.getValue(), String.valueOf(10.0 * 1000 * 1000));
        Map<String, String> data2 = new HashMap<>(quikAppData);
        data2.put(SUM_REPO.getValue(), String.valueOf(20.0 * 1000 * 1000));
        Map<String, String> data3 = new HashMap<>(quikAppData);
        data3.put(SUM_REPO.getValue(), String.valueOf(3.0 * 1000 * 1000));

        new AuctionSteps()
                .loginQuik(QUIK_PSB)
                .createAppRepo(bidInstrument, data1)
                .createAppRepo(bidInstrument, data2)
                .createAppRepo(bidInstrument, data3)
                .closeDesktop();
    }

    @Story(t250Name)
    @Test
    @Order(20)
    @Link(name = "SPB-T250", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t250Name + " 15:05")
    public void t_250_15_05() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(15).withMinute(5));

        String fileNamePfx02 = SpvbUtils.generateFileNameSaveIndex(PFX02, XML, BR_IN);
        String fileNamePfx12 = SpvbUtils.generateFileNameIncreaseIndex(PFX12, XML, BR_IN);

        SftpUtils.downloadFile(fileNamePfx02, BR_IN.getValue(), TEMP_FILES.getValue());
        int fixId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02, "//FIX/@FIX_ID"));

        String processName = String.format(PROCESSING_APP.getValue(),
                LocalTime.of(LocalTime.now().getHour(), 0)
                        .format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())));

        new AuctionSteps()
                .openChildProcess(String.format(BID_REPO_FIX.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(fileNamePfx12, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        10.0 * 1000 * 1000, "A",
                        20.0 * 1000 * 1000, "A",
                        3.0 * 1000 * 1000, "R"
                ))
                .sendXmlAndRename(fileNamePfx12)
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), PFX12_SEND_NOTIFICATION_TO_BR.getValue()), 5 * 60)
                .waitPfx12GetFromBrFinish(PFX12_GET_BR_REGISTRY_REPO.getValue(), fileNamePfx12.substring(0, fileNamePfx12.indexOf(".")))
                .openChildProcess(String.format(BID_REPO_FIX.getValue(), fixId), processName)
                .downloadRepoCbrFile()
                .closeWebDriver()
                .prepareAuctionCbrTriFile(bidInstrument)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .closeDesktop()
                .openChildProcess(String.format(BID_REPO_FIX.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .openChildProcess(processName, PFX13_SEND_REPO_TO_BR.getValue())
                .waitStepsFinished(List.of(), 5 * 60)
                .openChildProcess(processName, SEND_REPO_TO_NRD.getValue())
                .waitStepsFinished(List.of(), 5 * 60);
    }

    @Story(t313Name)
    @Test
    @Tag("br_repo")
    @Order(21)
    @Link(name = "SPB-T313", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t313Name + " 15:36")
    public void t_313_15_36() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(15).withMinute(30));
        Map<XmlAttribute, String> pfxData = Map.of(
                FIX_ID, PfxUtils.generateAuctionId(AutomatingBlock.REPO),
                FIRST_LEG_DATE, LocalDateTime.now().plusDays(7).format(ofPattern(XML_DATE_FORMAT.getValue())),
                BID_START_TIME, LocalTime.now().withHour(15).withMinute(43).format(ofPattern(XML_TIME_FORMAT.getValue())),
                BID_END_TIME, LocalTime.now().withHour(18).withMinute(25).format(ofPattern(XML_TIME_FORMAT.getValue()))
        );

        Map<String, String> data1 = new HashMap<>(quikAppData);
        data1.put(SUM_REPO.getValue(), String.valueOf(8 * 1000 * 1000));
        data1.put(CODE_CALC.getValue(), "S7");
        data1.put(TERM_REPO.getValue(), "9");

        String fileNamePfx02 = SpvbUtils.generateFileNameIncreaseIndex(FileName.PFX02, XML, BR_IN);
        String processName = String.format(PROCESSING_APP.getValue(),
                LocalTime.of(LocalTime.now().getHour(), 45)
                        .format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())));

        PfxUtils.createPfx02BidLrrx(fileNamePfx02, pfxData);
        int fixId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02, "//FIX_A/@FIX_ID"));
        new AuctionSteps()
                .setGetAppsTime(Map.of(
                        LocalTime.of(LocalTime.now().getHour(), 45)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), true,
                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 0)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), true,
                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 15)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), false,
                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 30)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), false,
                        LocalTime.of(LocalTime.now().plusHours(1).getHour(), 45)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), false,
                        LocalTime.of(LocalTime.now().plusHours(2).getHour(), 0)
                                .format(ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())), true
                ))
                .sendXmlAndRename(fileNamePfx02)
                .openProcess(PFX02_NOTIFICATION_AUCTION_REPO.getValue())
                .waitStepsFinished(List.of(fileNamePfx02.substring(0, fileNamePfx02.indexOf("."))), 3 * 60)
                .closeWebDriver()
                .refreshBroker()
                .waitTime(LocalTime.of(LocalTime.now().getHour(), 50, 0))
                .openChildProcess(String.format(BID_REPO_FLOAT.getValue(), fixId), processName)
                .checkListStepsOfProcess(List.of(IMPORT_UT_FROM_QUIK.getValue()))
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .loginQuik(QUIK_PSB)
                .createAppRepo(bidFloatInstrument, data1);

    }

    @Story(t313Name)
    @Test
    @Order(22)
    @Link(name = "SPB-T313", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t313Name + " 16:05")
    public void t_313_16_05() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(16).withMinute(5));
        String fileNamePfx02 = SpvbUtils.generateFileNameSaveIndex(PFX02, XML, BR_IN);
        String fileNamePfx12 = SpvbUtils.generateFileNameIncreaseIndex(PFX12, XML, BR_IN);

        SftpUtils.downloadFile(fileNamePfx02, BR_IN.getValue(), TEMP_FILES.getValue());
        int fixId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02, "//FIX_A/@FIX_ID"));
        String processName = String.format(PROCESSING_APP.getValue(),
                LocalTime.of(LocalTime.now().getHour(), 0)
                        .format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())));

        new AuctionSteps()
                .openChildProcess(String.format(BID_REPO_FLOAT.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(fileNamePfx12, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        8.0 * 1000 * 1000, "R"
                ))
                .sendXmlAndRename(fileNamePfx12)
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), PFX12_SEND_NOTIFICATION_TO_BR.getValue()), 5 * 60)
                .waitPfx12GetFromBrFinish(PFX12_GET_BR_REGISTRY_REPO.getValue(), fileNamePfx12.substring(0, fileNamePfx12.indexOf(".")))
                .openChildProcess(String.format(BID_REPO_FLOAT.getValue(), fixId), processName)
                .downloadRepoCbrFile()
                .closeWebDriver()
                .prepareAuctionCbrTriFile(bidFloatInstrument)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .closeDesktop()
                .openChildProcess(String.format(BID_REPO_FLOAT.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .openChildProcess(processName, PFX13_SEND_REPO_TO_BR.getValue())
                .waitStepsFinished(List.of(), 5 * 60)
                .openChildProcess(processName, SEND_REPO_TO_NRD.getValue())
                .waitStepsFinished(List.of(), 5 * 60);
    }

    @Story(t313Name)
    @Test
    @Order(24)
    @Link(name = "SPB-T313", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t313Name + " 16:46")
    public void t_313_16_46() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(16).withMinute(50));
        Map<String, String> data1 = new HashMap<>(quikAppData);
        data1.put(SUM_REPO.getValue(), String.valueOf(6.0 * 1000 * 1000));
        data1.put(CODE_CALC.getValue(), "S7");
        data1.put(TERM_REPO.getValue(), "9");
        data1.put(RATE_REPO.getValue(), "16");
        Map<String, String> data2 = new HashMap<>(data1);
        data2.put(SUM_REPO.getValue(), String.valueOf(5.0 * 1000 * 1000));
        Map<String, String> data3 = new HashMap<>(data1);
        data3.put(SUM_REPO.getValue(), String.valueOf(3.0 * 1000 * 1000));

        new AuctionSteps()
                .loginQuik(QUIK_PSB)
                .createAppRepo(bidFloatInstrument, data1)
                .createAppRepo(bidFloatInstrument, data2)
                .createAppRepo(bidFloatInstrument, data3)
                .closeDesktop();
    }

    @Story(t313Name)
    @Test
    @Order(25)
    @Link(name = "SPB-T313", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T313")
    @DisplayName(t313Name + " 17:05")
    public void t313_17_05() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(17).withMinute(5));
        String fileNamePfx02 = SpvbUtils.generateFileNameSaveIndex(PFX02, XML, BR_IN);
        String fileNamePfx12 = SpvbUtils.generateFileNameIncreaseIndex(PFX12, XML, BR_IN);
        SftpUtils.downloadFile(fileNamePfx02, BR_IN.getValue(), TEMP_FILES.getValue());
        int fixId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02, "//FIX_A/@FIX_ID"));

        String processName = String.format(PROCESSING_APP.getValue(),
                LocalTime.of(LocalTime.now().getHour(), 0)
                        .format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())));

        new AuctionSteps()
                .openChildProcess(String.format(BID_REPO_FLOAT.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_UT_FROM_QUIK.getValue()), 10 * 60)
                .waitStepsFinished(List.of(PFX11_VALIDATION_NOTIFICATION.getValue()), 15 * 60)
                .createPfx12(fileNamePfx12, PFX11_VALIDATION_NOTIFICATION.getValue(), Map.of(
                        6.0 * 1000 * 1000, "A",
                        5.0 * 1000 * 1000, "A",
                        3.0 * 1000 * 1000, "R"
                ))
                .sendXmlAndRename(fileNamePfx12)
                .waitStepsFinished(List.of(WAIT_PFX12.getValue(), PFX12_SEND_NOTIFICATION_TO_BR.getValue()), 5 * 60)
                .waitPfx12GetFromBrFinish(PFX12_GET_BR_REGISTRY_REPO.getValue(), fileNamePfx12.substring(0, fileNamePfx12.indexOf(".")));

    }

    @Story(t313Name)
    @Test
    @Order(26)
    @Link(name = "SPB-T313", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T250")
    @DisplayName(t313Name + " 17:05")
    public void t313_18_05() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(18).withMinute(10));
        String fileNamePfx02 = SpvbUtils.generateFileNameSaveIndex(PFX02, XML, BR_IN);
        SftpUtils.downloadFile(fileNamePfx02, BR_IN.getValue(), TEMP_FILES.getValue());
        int fixId = Integer.parseInt(SpvbUtils.getAucId(TEMP_FILES.getValue() + fileNamePfx02, "//FIX_A/@FIX_ID"));

        String processName = String.format(PROCESSING_APP.getValue(),
                LocalTime.of(LocalTime.now().getHour() - 1, 0)
                        .format(DateTimeFormatter.ofPattern(PROCESSING_APP_TIME_FORMAT.getValue())));


        new AuctionSteps()
                .openChildProcess(String.format(BID_REPO_FLOAT.getValue(), fixId), processName)
                .downloadRepoCbrFile()
                .closeWebDriver()
                .prepareAuctionCbrTriFile(bidFloatInstrument)
                .prepareCbrTroFile()
                .processDynamicTransaction()
                .closeDesktop()
                .openChildProcess(String.format(BID_REPO_FLOAT.getValue(), fixId), processName)
                .waitStepsFinished(List.of(IMPORT_REPO_FROM_QUIK.getValue()), 5 * 60)
                .openChildProcess(processName, PFX13_SEND_REPO_TO_BR.getValue())
                .waitStepsFinished(List.of(), 5 * 60)
                .openChildProcess(processName, SEND_REPO_TO_NRD.getValue())
                .waitStepsFinished(List.of(), 5 * 60);
    }

    @Test
    @Order(27)
    @Link(name = "SPB-T314", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T314")
    @DisplayName("SPB-T314. PFX13 (итоговый) после формирования сделок. 18:40")
    public void t314_18_40() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(18).withMinute(40));

        int startHH = 18;
        int startMin = 45;
        int startSec = 0;
        int endHH = 19;
        int endMin = 0;
        int endSec = 0;


        AuctionSteps auctionSteps = new AuctionSteps()
                .openProcessWithPlannedStart(PFX13_SEND_FINAL_REPO_TO_BR,
                        LocalTime.of(startHH, startMin, startSec))
                .waitStepsFinished(pfx13steps, 10 * 60);

        String namePfx13 = SpvbUtils.getFileNameCreatedBetween(PFX13, ".xml",
                BR_OUT_NO_SIGN,
                LocalDateTime.of(LocalDate.now(), LocalTime.of(startHH, startMin, startSec)),
                LocalDateTime.of(LocalDate.now(), LocalTime.of(endHH, endMin + 1, endSec)));

        auctionSteps.checkFinalPfx13(namePfx13);
    }

    @Test
    @Order(28)
    @Link(name = "SPB-T315", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T315")
    @DisplayName("SPB-T315. Проверка успешного завершения торгов РЕПО по фикс. и плав ставке. 19:40")
    public void t315_19_40() {
        Credentials.setEnv("test");

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(19).withMinute(40));

        new AuctionSteps()
                .checkProcessStatus("Торги по фиксированной ставке", Status.COMPLETE)
                .checkProcessStatus("Торги по плавающей ставке РЕПО (FIX_ID: ", Status.COMPLETE);
    }
}
