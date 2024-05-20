package ru.spvb.auction;

import constants.*;
import elements.columns.BankRussiaProcessColumn;
import io.qameta.allure.Epic;
import io.qameta.allure.Link;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import ru.spvb.auction.common.BaseTest;
import ru.spvb.steps.auction.AuctionSteps;
import utils.SpvbUtils;
import utils.WaitingUtils;
import utils.XmlUtils;

import java.time.LocalDateTime;
import java.util.*;

import static constants.DateFormat.XML_DATE_FORMAT;
import static constants.Extension.*;
import static constants.FileName.*;
import static constants.FilePath.*;
import static java.time.format.DateTimeFormatter.ofPattern;

@Epic("Модуль банка России (аукционы ОФЗ + РЕПО)")
@SuppressWarnings("UnnecessaryLocalVariable")
public class AuctionTest extends BaseTest {

    private final String ENV = "test";
    private final String CLIENT_CODE = "589";

    public Map<String, String> prepareAuctionMap() {
        Map<String, String> priceQuantity = new TreeMap<>(Comparator.reverseOrder());
        priceQuantity.put("99", "10000");
        priceQuantity.put("98", "10000");
        priceQuantity.put("97", "10000");
        return priceQuantity;
    }

    private static @NotNull List<List<Map<String, String>>> getAttrs() {

        Map<String, String> SU26241RMFS8_1 = new HashMap<>();
        SU26241RMFS8_1.put("BOARDID", "BMFC");

        Map<String, String> SU26241RMFS8_2 = new HashMap<>();
        SU26241RMFS8_2.put("SECURITYID", "SU26241RMFS8");

        Map<String, String> SU26241RMFS8_3 = new HashMap<>();
        SU26241RMFS8_3.put("PRICE", "99.000000");
        SU26241RMFS8_3.put("QUANTITY", "10000");

        Map<String, String> SU26241RMFS8_4 = new HashMap<>();
        SU26241RMFS8_4.put("PRICE", "98.000000");
        SU26241RMFS8_4.put("QUANTITY", "6000");

        Map<String, String> SU26241RMFS8_5 = new HashMap<>();
        SU26241RMFS8_5.put("PFF63_COUNT", "2");

        Map<String, String> SU26241RMFS8_6 = new HashMap<>();
        SU26241RMFS8_6.put("BOARDID", "SMFC");

        Map<String, String> SU26241RMFS8_7 = new HashMap<>();
        SU26241RMFS8_7.put("SECURITYID", "SU26241RMFS8");

        Map<String, String> SU26241RMFS8_8 = new HashMap<>();
        SU26241RMFS8_8.put("PRICE", "98.000000");
        SU26241RMFS8_8.put("QUANTITY", "4000");

        Map<String, String> SU26241RMFS8_9 = new HashMap<>();
        SU26241RMFS8_9.put("PFF63_COUNT", "1");


        Map<String, String> SU26243RMFS4_1 = new HashMap<>();
        SU26243RMFS4_1.put("BOARDID", "BMFC");

        Map<String, String> SU26243RMFS4_2 = new HashMap<>();
        SU26243RMFS4_2.put("SECURITYID", "SU26243RMFS4");

        Map<String, String> SU26243RMFS4_3 = new HashMap<>();
        SU26243RMFS4_3.put("PRICE", "99.000000");
        SU26243RMFS4_3.put("QUANTITY", "10000");

        Map<String, String> SU26243RMFS4_4 = new HashMap<>();
        SU26243RMFS4_4.put("PRICE", "98.000000");
        SU26243RMFS4_4.put("QUANTITY", "6000");

        Map<String, String> SU26243RMFS4_5 = new HashMap<>();
        SU26243RMFS4_5.put("PFF63_COUNT", "2");

        Map<String, String> SU26243RMFS4_6 = new HashMap<>();
        SU26243RMFS4_6.put("BOARDID", "SMFC");

        Map<String, String> SU26243RMFS4_7 = new HashMap<>();
        SU26243RMFS4_7.put("SECURITYID", "SU26243RMFS4");

        Map<String, String> SU26243RMFS4_8 = new HashMap<>();
        SU26243RMFS4_8.put("PRICE", "97.000000");
        SU26243RMFS4_8.put("QUANTITY", "10000");

        Map<String, String> SU26243RMFS4_9 = new HashMap<>();
        SU26243RMFS4_9.put("PRICE", "97.000000");
        SU26243RMFS4_9.put("QUANTITY", "4000");

        Map<String, String> SU26243RMFS4_10 = new HashMap<>();
        SU26243RMFS4_10.put("PFF63_COUNT", "2");


        Map<String, String> SU26244RMFS2_1 = new HashMap<>();
        SU26244RMFS2_1.put("BOARDID", "BMFC");

        Map<String, String> SU26244RMFS2_2 = new HashMap<>();
        SU26244RMFS2_2.put("SECURITYID", "SU26244RMFS2");

        Map<String, String> SU26244RMFS2_3 = new HashMap<>();
        SU26244RMFS2_3.put("PRICE", "99.000000");
        SU26244RMFS2_3.put("QUANTITY", "10000");

        Map<String, String> SU26244RMFS2_4 = new HashMap<>();
        SU26244RMFS2_4.put("PRICE", "98.000000");
        SU26244RMFS2_4.put("QUANTITY", "6000");

        Map<String, String> SU26244RMFS2_5 = new HashMap<>();
        SU26244RMFS2_5.put("PFF63_COUNT", "2");

        Map<String, String> SU26244RMFS2_6 = new HashMap<>();
        SU26244RMFS2_6.put("BOARDID", "SMFC");

        Map<String, String> SU26244RMFS2_7 = new HashMap<>();
        SU26244RMFS2_7.put("SECURITYID", "SU26244RMFS2");

        Map<String, String> SU26244RMFS2_8 = new HashMap<>();
        SU26244RMFS2_8.put("PFF63_COUNT", "0");


        Map<String, String> SU26242RMFS6_1 = new HashMap<>();
        SU26242RMFS6_1.put("BOARDID", "BMFC");

        Map<String, String> SU26242RMFS6_2 = new HashMap<>();
        SU26242RMFS6_2.put("SECURITYID", "SU26242RMFS6");

        Map<String, String> SU26242RMFS6_3 = new HashMap<>();
        SU26242RMFS6_3.put("PRICE", "99.000000");
        SU26242RMFS6_3.put("QUANTITY", "10000");

        Map<String, String> SU26242RMFS6_4 = new HashMap<>();
        SU26242RMFS6_4.put("PRICE", "98.000000");
        SU26242RMFS6_4.put("QUANTITY", "6000");

        Map<String, String> SU26242RMFS6_5 = new HashMap<>();
        SU26242RMFS6_5.put("PFF63_COUNT", "2");

        Map<String, String> SU26242RMFS6_6 = new HashMap<>();
        SU26242RMFS6_6.put("BOARDID", "SMFC");

        Map<String, String> SU26242RMFS6_7 = new HashMap<>();
        SU26242RMFS6_7.put("SECURITYID", "SU26242RMFS6");

        Map<String, String> SU26242RMFS6_8 = new HashMap<>();
        SU26242RMFS6_8.put("PFF63_COUNT", "0");


        Map<String, String> SU26239RMFS2_1 = new HashMap<>();
        SU26239RMFS2_1.put("BOARDID", "BMFC");

        Map<String, String> SU26239RMFS2_2 = new HashMap<>();
        SU26239RMFS2_2.put("SECURITYID", "SU26239RMFS2");

        Map<String, String> SU26239RMFS2_3 = new HashMap<>();
        SU26239RMFS2_3.put("PRICE", "96.000000");
        SU26239RMFS2_3.put("QUANTITY", "10000");

        Map<String, String> SU26239RMFS2_4 = new HashMap<>();
        SU26239RMFS2_4.put("PRICE", "95.000000");
        SU26239RMFS2_4.put("QUANTITY", "20000");

        Map<String, String> SU26239RMFS2_5 = new HashMap<>();
        SU26239RMFS2_5.put("PFF63_COUNT", "2");



        Map<String, String> SU26238RMFS4_1 = new HashMap<>();
        SU26238RMFS4_1.put("BOARDID", "BMFC");

        Map<String, String> SU26238RMFS4_2 = new HashMap<>();
        SU26238RMFS4_2.put("SECURITYID", "SU26238RMFS");

        Map<String, String> SU26238RMFS4_3 = new HashMap<>();
        SU26238RMFS4_3.put("PFF63_COUNT", "0");


        Map<String, String> SU29012RMF10_1 = new HashMap<>();
        SU29012RMF10_1.put("BOARDID", "BMFC");

        Map<String, String> SU29012RMF10_2 = new HashMap<>();
        SU29012RMF10_2.put("SECURITYID", "SU29012RMF10");

        Map<String, String> SU29012RMF10_3 = new HashMap<>();
        SU29012RMF10_3.put("PFF63_COUNT", "0");

        return List.of(
                List.of(SU26241RMFS8_1, SU26241RMFS8_2, SU26241RMFS8_3, SU26241RMFS8_5),
                List.of(SU26241RMFS8_1, SU26241RMFS8_2, SU26241RMFS8_4, SU26241RMFS8_5),
                List.of(SU26241RMFS8_6, SU26241RMFS8_7, SU26241RMFS8_8, SU26241RMFS8_9),
                List.of(SU26243RMFS4_1, SU26243RMFS4_2, SU26243RMFS4_3, SU26243RMFS4_5),
                List.of(SU26243RMFS4_1, SU26243RMFS4_2, SU26243RMFS4_4, SU26243RMFS4_5),
                List.of(SU26243RMFS4_6, SU26243RMFS4_7, SU26243RMFS4_8, SU26243RMFS4_10),
                List.of(SU26243RMFS4_6, SU26243RMFS4_7, SU26243RMFS4_9, SU26243RMFS4_10),
                List.of(SU26239RMFS2_1, SU26239RMFS2_2, SU26239RMFS2_3, SU26239RMFS2_5),
                List.of(SU26239RMFS2_1, SU26239RMFS2_2, SU26239RMFS2_4, SU26239RMFS2_5),
                List.of(SU26244RMFS2_1, SU26244RMFS2_2, SU26244RMFS2_3, SU26244RMFS2_5),
                List.of(SU26244RMFS2_1, SU26244RMFS2_2, SU26244RMFS2_4, SU26244RMFS2_5),
                List.of(SU26244RMFS2_6, SU26244RMFS2_7, SU26244RMFS2_8, SU26244RMFS2_8),
                List.of(SU26242RMFS6_1, SU26242RMFS6_2, SU26242RMFS6_3, SU26242RMFS6_5),
                List.of(SU26242RMFS6_6, SU26242RMFS6_7, SU26242RMFS6_8, SU26242RMFS6_8),
                List.of(SU26238RMFS4_1, SU26238RMFS4_2, SU26238RMFS4_3, SU26238RMFS4_3),
                List.of(SU29012RMF10_1, SU29012RMF10_2, SU29012RMF10_3, SU29012RMF10_3),
                List.of(SU26242RMFS6_1, SU26242RMFS6_2, SU26242RMFS6_4, SU26242RMFS6_5)
        );
    }

    @Test
    @Order(0)
    @Link(name = "SPB-T217", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T217")
    @DisplayName("SPB-T217. Размещение PFX41 и создание таблицы текущих торгов - 09:00")
    public void spb_t217() {
        Credentials.setEnv(ENV);
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(9).withMinute(10));
        String pfx41Name = SpvbUtils.generateFileNameIncreaseIndex(PFX41, XML, BR_IN);

        AuctionSteps auctionSteps = new AuctionSteps()
                .createPfx41(pfx41Name)
                .copyFile(FilePath.TEMP_FILES.getValue() + pfx41Name)
                .signFile(BR_IN.getValue() + pfx41Name)
                .renameFile(BR_IN.getValue(), pfx41Name + SIG.getValue(), pfx41Name + P7S.getValue());

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(9).withMinute(21).withSecond(0));
        auctionSteps
                .sendZFiles(ZPFX38, 8)
                .sendZFiles(ZPFX43, 12)
                .sendZFiles(ZPFX63, 12)
                .sendZFiles(ZPFF63, 3);

        WaitingUtils.waitUntil(LocalDateTime.now().withHour(9).withMinute(31).withSecond(0));
        auctionSteps
                .checkPfx41Status(pfx41Name)
                .checkAuctionsExist(pfx41Name)
                .closeWebDriver();
    }

    @Test
    @Order(1)
    @Link(name = "SPB-T96", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T96")
    @DisplayName("SPB-T96. Аукцион ОФЗ - заявки не поданы (№5) - 10:09")
    public void spb_t96() {
        Credentials.setEnv(ENV);
        String stockCode = "SU29012RMF10";

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(PFX41, "START_CUT_TIME", stockCode));
        new AuctionSteps()
                .checkPfx38Status(stockCode)
                .checkSuccessAuctionStatus(stockCode)
                .closeWebDriver();
    }

    @Test
    @Order(2)
    @Link(name = "SPB-T97", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T97")
    @DisplayName("SPB-T97. Аукцион ОФЗ - заявки отклонены (№6) - 10:25")
    public void spb_t97() {
        Credentials.setEnv(ENV);
        String stockCode = "SU26238RMFS4";
        String stockName = "ОФЗ 26238";

        Map<String, String> priceQuantity = new TreeMap<>(Comparator.reverseOrder());
        priceQuantity.put("91", "20000");
        priceQuantity.put("90", "10000");

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(PFX41, "START_TIME", stockCode));

        AuctionSteps auctionSteps = new AuctionSteps()
                .loginQuik(QUIK_VTB)
                .sendRequestByQuik(stockName, priceQuantity, CLIENT_CODE);

        SpvbUtils.cleanQuikFiles();

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(PFX41, "START_CUT_TIME", stockCode));
        String pfx38Name = SpvbUtils.generateFileNameSaveIndex(PFX38, XML, BR_OUT_NO_SIGN);
        String pfx39Name = SpvbUtils.generateFileNameIncreaseIndex(PFX39, XML, BR_IN);

        auctionSteps
                .checkPfx38Status(stockCode)
                .createPfx39_t97(pfx38Name, pfx39Name)
                .copyFile(FilePath.TEMP_FILES.getValue() + pfx39Name)
                .signFile(BR_IN.getValue() + pfx39Name)
                .renameFile(BR_IN.getValue(), pfx39Name + SIG.getValue(), pfx39Name + P7S.getValue())
                .checkPfx39Status(pfx39Name.substring(0, pfx39Name.lastIndexOf('.')), stockCode)
                .checkSuccessAuctionStatus(stockCode)
                .closeWebDriver();
    }

    @Test
    @Order(3)
    @Link(name = "SPB-T87", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T87")
    @DisplayName("SPB-T87. Аукцион ОФЗ - заявки фулл исполнены (№8) - 10:52")
    public void spb_t87() {
        Credentials.setEnv(ENV);
        String stockCode = "SU26239RMFS2";
        String stockName = "ОФЗ 26239";

        Map<String, String> priceQuantity = new TreeMap<>(Comparator.reverseOrder());
        priceQuantity.put("96", "10000");
        priceQuantity.put("95", "20000");

        new AuctionSteps().holdAuction(stockName, stockCode, CLIENT_CODE, priceQuantity, 2);
    }


    @Test
    @Order(4)
    @Link(name = "SPB-T113", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T113")
    @DisplayName("SPB-T113. Доразмещение + аукцион. Классика. Сокращение времени с 18-00 до 12-00 (№1) - 11:20")
    public void spb_t113() {
        Credentials.setEnv(ENV);
        String stockCode = "SU26241RMFS8";
        String stockName = "ОФЗ 26241";
        FileName pfx42_1 = PFX42_T113;

        new AuctionSteps()
                .holdAuction(stockName, stockCode, CLIENT_CODE, prepareAuctionMap(), 2)
                .holdExtraReplacement(stockCode, 1, pfx42_1, true, CLIENT_CODE);

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(pfx42_1, "END_CUT_TIME", stockCode).plusMinutes(5));
        new AuctionSteps().checkExtraReplacementStatus(stockCode);
    }

    @Test
    @Order(5)
    @Link(name = "SPB-T189", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T189")
    @DisplayName("SPB-T189. Доразмещение + аукцион (увеличили LIMIT PRICE) (№2) - 12:40")
    public void spb_t189() {
        Credentials.setEnv(ENV);
        String stockCode = "SU26242RMFS6";
        String stockName = "ОФЗ 26242";
        FileName pfx42_1 = PFX42_T189_1;
        FileName pfx42_2 = PFX42_T189_2;

        new AuctionSteps()
                .holdAuction(stockName, stockCode, CLIENT_CODE, prepareAuctionMap(), 2)
                .holdExtraReplacement(stockCode, 0, pfx42_1, pfx42_2, false, CLIENT_CODE);

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(pfx42_2, "END_CUT_TIME", stockCode).plusMinutes(5));
        new AuctionSteps().checkExtraReplacementStatus(stockCode);
    }

    @Test
    @Order(6)
    @Link(name = "SPB-T95", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T95")
    @DisplayName("SPB-T95. Доразмещение + аукцион без обрезки времени (END_CUT_TIME = 14:37) (№3) - 14:20")
    public void spb_t95() {
        Credentials.setEnv(ENV);
        String stockCode = "SU26243RMFS4";
        String stockName = "ОФЗ 26243";
        FileName pfx42_1 = PFX42_T95_1;
        FileName pfx42_2 = PFX42_T95_2;

        new AuctionSteps()
                .holdAuction(stockName, stockCode, CLIENT_CODE, prepareAuctionMap(), 2)
                .holdExtraReplacement(stockCode, 2, pfx42_1, pfx42_2, true, CLIENT_CODE);

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(pfx42_2, "END_CUT_TIME", stockCode).plusMinutes(5));
        new AuctionSteps().checkExtraReplacementStatus(stockCode);
    }

    @Test
    @Order(7)
    @Link(name = "SPB-T197", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T197")
    @DisplayName("SPB-T197. Доразмещение + аукцион (участник не подал встречные заявки) (№4) - 16:25")
    public void spb_t197() {
        Credentials.setEnv(ENV);
        String stockCode = "SU26244RMFS2";
        String stockName = "ОФЗ 26244";
        FileName pfx42_1 = PFX42_T197;

        new AuctionSteps()
                .holdAuction(stockName, stockCode, CLIENT_CODE, prepareAuctionMap(), 2)
                .holdExtraReplacement(stockCode, 2, pfx42_1, false, CLIENT_CODE);

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(pfx42_1, "END_CUT_TIME", stockCode).plusMinutes(5));
        new AuctionSteps().checkExtraReplacementStatus(stockCode);
    }


    @Test
    @Order(8)
    @Link(name = "SPB-T98", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T98")
    @DisplayName("SPB-T98. Аукцион ОФЗ - заявки проигнорированы (№7) - 17:40")
    public void spb_t98() {
        Credentials.setEnv(ENV);
        String stockName = "TEST13";
        String stockCode = "SU29012RMF13";

        Map<String, String> priceQuantity = new TreeMap<>(Comparator.reverseOrder());
        priceQuantity.put("99", "10000");
        priceQuantity.put("98", "20000");
        priceQuantity.put("97", "30000");

        WaitingUtils.waitUntil(XmlUtils.getTimeFromPfx(PFX41, "START_TIME", stockCode));
        AuctionSteps auctionSteps = new AuctionSteps()
                .loginQuik(QUIK_VTB)
                .sendRequestByQuik(stockName, priceQuantity, CLIENT_CODE);

        WaitingUtils.waitUntil(auctionSteps.getTimeFromUi(stockCode, BankRussiaProcessColumn.PLANNED_END_TIME));

        auctionSteps
                .checkPfx38Status(stockCode)
                .checkFailAuctionStatus(stockCode);
    }

    @Test
    @Order(9)
    @Link(name = "SPB-T149", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T149")
    @DisplayName("SPB-T149. PFX64 - сигнал от КС не получен /// 18:20 - 18:25")
    public void spb_t149() {
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(18).withMinute(18).withSecond(0));
        Credentials.setEnv(ENV);
        String processName = ProcessName.PFX64_SEND_TO_BR.getValue();
        LocalDateTime startTime = LocalDateTime.now().withHour(18).withMinute(20).withSecond(0);
        String operationName = "PFX64_FROM_KS";
        String message = "Не удалось получить файл для формирования PFX64 из КС. Ошибка: Не получен сигнал от КС";

        new AuctionSteps().checkPfx64_65(processName, operationName, startTime, message);
    }

    @Test
    @Order(10)
    @Link(name = "SPB-T328", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T328")
    @DisplayName("SPB-T328. PFX65 - сигнал от КС не получен /// 18:26 - 18:30")
    public void spb_t328() {
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(18).withMinute(24).withSecond(0));
        Credentials.setEnv(ENV);
        String processName = ProcessName.PFX65_SEND_TO_BR.getValue();
        LocalDateTime startTime = LocalDateTime.now().withHour(18).withMinute(26).withSecond(0);
        String operationName = "PFX65_FROM_KS";
        String message = "Не удалось получить файл для формирования PFX65 из КС. Ошибка: Не получен сигнал от КС";

        new AuctionSteps().checkPfx64_65(processName, operationName, startTime, message);
    }

    @Test
    @Order(11)
    @Link(name = "SPB-T327", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T327")
    @DisplayName("SPB-T327. PFF63 (итоговый) после формирования сделок /// Старт 18:40")
    public void spb_t327() {
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(18).withMinute(45).withSecond(0));
        Credentials.setEnv(ENV);
        String processName = ProcessName.PFF_63_SEND_TO_BR.getValue();
        String containsText = "PFF63";
        int countSteps = 4;
        LocalDateTime plannedStartTime = LocalDateTime.now().withHour(18).withMinute(40).withSecond(0);
        LocalDateTime endTime = LocalDateTime.now().withHour(19).withMinute(0).withSecond(0);

        String pff63Name = SpvbUtils.getFileNameCreatedBetween(PFF63, ".xml",
                BR_OUT_NO_SIGN, plannedStartTime, endTime);

        Map<String, String> pff63TagAttrs = Map.of(
                "TRADEDATE", LocalDateTime.now().format(ofPattern(XML_DATE_FORMAT.getValue())),
                "EXCHANGE", "АО СПВБ",
                "FIRMNAME", "Банк России",
                "FIRMID", "9");

        List<List<Map<String, String>>> attrs = getAttrs();

        List<String> expectedAttrs = List.of("RECNUM", "TRADENO", "TRADETIME", "BUYSELL", "VALUE", "CPFIRMID", "SETTLECODE",
                "CLEARINGHOUSE", "ORDERNO", "ORDERTYPE");

        new AuctionSteps()
                .loginWeb(MenuTab.BANK_RUSSIA)
                .waitProcessCountStepsFinished(processName, containsText, countSteps, Status.COMPLETE.getValue())
                .checkPff63Tag(pff63Name, pff63TagAttrs, attrs, expectedAttrs)
                .checkProcessStatus(processName, Status.COMPLETE, plannedStartTime);
    }

    @Test
    @Order(12)
    @Link(name = "SPB-T133", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T133")
    @DisplayName("SPB-T133. Очистка SFTP модуля БР ///20:29 - 20:41")
    public void spb_t133() {
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(20).withMinute(28).withSecond(0));
        Credentials.setEnv(ENV);
        List<String> allowedStatuses = List.of(Status.START.getValue(), Status.SUCCESS.getValue());
        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        new AuctionSteps()
                .loginWeb(MenuTab.BANK_RUSSIA)
                .checkProcessStatus(ProcessName.SFTP_CLEAN.getValue(), Status.IN_PROGRESS, LocalDateTime.now().withHour(20).withMinute(30).withSecond(0))
                .checkProcessStatus(ProcessName.SFTP_CLEAN.getValue(), Status.COMPLETE, LocalDateTime.now().withHour(20).withMinute(30).withSecond(0))
                .checkBrInEmpty()
                .checkBrOutEmpty()
                .openAudit(startTime)
                .findOperation("BR_DOCS_DELETE")
                .checkAuditStatus(allowedStatuses)
                .findOperation("BR_OUT_DOCS_SAVE_TO_ATTACHMENT")
                .checkAuditStatus(allowedStatuses)
                .findOperation("BR_IN_DOCS_SAVE_TO_ATTACHMENT")
                .checkAuditStatus(allowedStatuses);
    }

}
