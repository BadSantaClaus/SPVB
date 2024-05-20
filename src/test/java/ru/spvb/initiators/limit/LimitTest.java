package ru.spvb.initiators.limit;

import constants.Credentials;
import constants.DateFormat;
import constants.DocStatus;
import io.qameta.allure.Epic;
import io.qameta.allure.Link;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import page.web.initiators.limits.LimitsPage;
import page.web.initiators.limits.OpenedLimitPage;
import ru.spvb.auction.common.BaseTest;
import ru.spvb.steps.limit.LimitData;
import ru.spvb.steps.limit.LimitSteps;
import utils.LimUtils;
import utils.SftpUtils;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static ru.spvb.steps.limit.LimitSteps.getLastName;
import static ru.spvb.steps.limit.LimitSteps.getWebData;

@Epic("Инициаторы: лимиты")
@Order(1)
public class LimitTest extends BaseTest {
    @Test
    @Order(1)
    @Tag("CI2")
    @Link(name = "SPB-T4", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T4")
    @DisplayName("SPB-T4. Экспорт лимитов МКР в торговую систему. Инициатор КФ ЛО")
    public void SPB_T4Test() {
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(9).withMinute(30));
        Credentials.setEnv("test");
        List<String> remoteFiles = new SftpUtils().getAllFileNamesInDirectory("report/users/out/" +
                LocalDateTime.now().format(DateTimeFormatter
                        .ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/DBF");
        File file = new File("src/test/resources/Limits/test1_1.pdf");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadMKR("КФ ЛО", file)
                .documentStatus(getLastName(SpvbUtils.getFileExtension(file.getPath())), DocStatus.NEW, new LimitsPage())
                .openLimitPage(getLastName(SpvbUtils.getFileExtension(file.getPath())))
                .setLotRubleCheckbox(false)
                .setCalculationsTag("KFLO")
                .addYTLimits(LimitData.originalDataTest1_1())
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .checkWebData(getWebData(), LimitData.originalDataTest1_1())
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .sendLimits()
                .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                .dateExportToLKExists()
                .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                        LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test1_1.lim")))
                .QExportCheck("KFLO", LimitData.originalDataTest1_1(),6)
                .sftpUploadCheck(remoteFiles,
                        new SftpUtils().getAllFileNamesInDirectory("report/users/out/" +
                                LocalDateTime.now().format(DateTimeFormatter
                                        .ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/DBF"),
                        "45", "dbf");
    }

    @Test
    @Tag("CI2")
    @Order(2)
    @Link(name = "SPB-T3", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T3")
    @DisplayName("SPB-T3. Экспорт Лимитов МКР в торговую систему. Инициатор - КФ СПБ")
    public void SPB_T3Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/mkr-t-limits-12022024(ТК_1.2).dbf");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadMKR("T", file)
                .openLimitPage(getLastName("dbf"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .checkWebData(getWebData(), LimitData.originalDataTest1_2())
                .setInstrumentSpec("КФ СПБ")
                .setCalculationsTag("KFSP")
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .sendLimits()
                .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                .dateExportToLKExists()
                .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                        LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test1_2.lim")))
                .QExportCheck("KFSP", LimitData.originalDataTest1_2(), 18);

    }

    @Test
    @Tag("CI2")
    @Order(3)
    @SneakyThrows
    @Link(name = "SPB-T12", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T12")
    @DisplayName("SPB-T12. Загрузка документа лимитов МКР. Инициатор - Федеральное казначейство (K)")
    public void SPB_T12Test() {
        Credentials.setEnv("test");
        List<String> remoteFiles;
        try {
            remoteFiles = new SftpUtils().getAllFileNamesInDirectory("report/users/out/" +
                    LocalDateTime.now().format(DateTimeFormatter
                            .ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/XML");
        } catch (Throwable throwable) {
            remoteFiles = new ArrayList<>();
        }
        File file = new File("src/test/resources/Limits/MKR_K_LIMITS_11012024_001(ТК_1.3).xml");
        File newFile = new File("src/test/resources/Limits/temp/" + LimUtils.generateName("mkr", "k", "xml"));
        FileUtils.copyFile(file, newFile);
        try {
            new LimitSteps()
                    .uploadSftp(newFile, "initiators/mkr/K/")
                    .openSiteGoToLimitsSection()
                    .openLimitPage(newFile.getName())
                    .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                    .checkWebData(getWebData(), LimitData.originalDataTest1_3())
                    .setLotRubleCheckbox(false)
                    .setCalculationsTag("FKRF")
                    .exportLimits()
                    .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                    .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                    .dateExportToTCExists()
                    .sendLimits()
                    .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                    .dateExportToLKExists()
                    .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                            LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test1_3.lim")))
                    .QExportCheck("FKRF", LimitData.originalDataTest1_3(), 1)
                    .sftpUploadCheck(remoteFiles,
                            new SftpUtils().getAllFileNamesInDirectory("report/users/out/" +
                                    LocalDateTime.now().format(DateTimeFormatter
                                            .ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + "/XML"),
                            "472", "xml");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            newFile.delete();
        }
    }

    @Test
    @Tag("CI2")
    @Order(4)
    @Link(name = "SPB-T11", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T11")
    @DisplayName("SPB-11. Загрузка документа лимитов МКР. Инициатор - ГКР \"ВЭБ.РФ\" (VEB)")
    public void SPB_T11Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/MKR_VEB_LIMITS_25012024_001(ТК_1_4).xml");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadMKR("ВЭБ.РФ", file)
                .openLimitPage(getLastName("xml"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .checkWebData(getWebData(), LimitData.originalDataTest1_4())
                .setLotRubleCheckbox(false)
                .setCalculationsTag("VBRF")
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .sendLimits()
                .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                .dateExportToLKExists()
                .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                        LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test1_4.lim")));
    }

    @Test
    @Tag("CI2")
    @Order(5)
    @Link(name = "SPB-T14", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T14")
    @DisplayName("SPB-T14. Экспорт Лимитов МКР в торговую систему. Инициатор - \"ФСКМСБ МKК")
    public void SPB_T14Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/MKR_M_LIMITS_05032024_002(ТК_1.6).dbf");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadMKR("M", file)
                .openLimitPage(getLastName("dbf"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .checkWebData(getWebData(), LimitData.originalDataTest1_6())
                .setLotRubleCheckbox(false)
                .setCalculationsTag("FSKB")
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .sendLimits()
                .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                .dateExportToLKExists()
                .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                        LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test1_6.lim")));
    }

    @Test
    @Tag("CI2")
    @Order(6)
    @Link(name = "SPB-T278", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T278")
    @DisplayName("SPB-T278. Экспорт Лимитов РЕПО в торговую систему. Инициатор - «КФ ЛО»")
    public void SPB_T278Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/REPO_LO_LIMITS_19122023_003(TК_2.1).xml");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadREPO("КФ ЛО", file)
                .openLimitPage(getLastName("xml"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .checkWebData(getWebData(), LimitData.originalDataTest2_1())
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .sendLimits()
                .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                .dateExportToLKExists()
                .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                        LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test2_1.lim")));
    }

    @Test
    @Tag("CI2")
    @Order(7)
    @Link(name = "SPB-T279", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T279")
    @DisplayName("SPB-T279. Экспорт Лимитов РЕПО в торговую систему. Инициатор - «T (Комитет финансов СПб)»")
    public void SPB_T279Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/REPO_SP_LIMITS_11012024_007(ТК_2.2).xml");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadREPO("Комитет финансов СПб", file)
                .openLimitPage(getLastName("xml"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .checkWebData(getWebData(), LimitData.originalDataTest2_2())
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .sendLimits()
                .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                .dateExportToLKExists()
                .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                        LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test2_2.lim")));
    }

    @Test
    @Tag("CI2")
    @Order(8)
    @Link(name = "SPB-T280", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T280")
    @DisplayName("SPB-T280. Негативный. Проверка ошибки \"обрабатывается другой документ в статусе 'Экспортирован в ТС'\"")
    public void SPB_T280Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/mkr-t-limits-12022024(ТК_3.1).dbf");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadMKR("T", file);
        String name1 = getLastName("dbf");
        new LimitSteps()
                .openLimitPage(getLastName("dbf"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .checkWebData(getWebData(), LimitData.originalDataTest3_1())
                .setLotRubleCheckbox(false)
                .setCalculationsTag("KFSP")
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .navigateToLimitSection()
                .uploadMKR("T", file)
                .openLimitPage(getLastName("dbf"))
                .checkWebData(getWebData(), LimitData.originalDataTest3_1())
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .setLotRubleCheckbox(false)
                .setCalculationsTag("KFSP")
                .exportLimits()
                .exportError()
                .decline()
                .documentStatus(DocStatus.DECLINED, OpenedLimitPage.getInstance())
                .navigateToLimitSection()
                .openLimitPage(name1)
                .decline()
                .documentStatus(DocStatus.DECLINED, OpenedLimitPage.getInstance());
    }

    @Test
    @Tag("CI2")
    @Order(9)
    @Link(name = "SPB-T281", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T281")
    @DisplayName("SPB-T281. Негативный. Загрузка некорректного файла лимитов МКР. Инициатор - ФСКМБ")
    public void SPB_T281Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/mkr-m-limits-23012024-001(ТК_3_2).dbf");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadMKR("ФСКМСБ", file)
                .documentStatus(getLastName(SpvbUtils.getFileExtension(file.getPath())), DocStatus.ERROR, new LimitsPage())
                .openLimitPage(getLastName(SpvbUtils.getFileExtension(file.getPath())))
                .noContent()
                .decline()
                .documentStatus(DocStatus.DECLINED, OpenedLimitPage.getInstance());
    }

    @Test
    @Tag("CI2")
    @Order(10)
    @Link(name = "SPB-T282", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T282")
    @DisplayName("SPB-T282. Загрузка большого файла лимитов МКР. Инициатор - КФ СПБ")
    public void SPB_T282Test() {
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/МНОГО ЛИМИТОВ - T(ТК_3.3).dbf");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadMKR("Комитет финансов СПб", file)
                .openLimitPage(getLastName("dbf"))
                .checkWebData(getWebData(), LimitData.originalDataTest3_3())
                .setLotRubleCheckbox(false)
                .setCalculationsTag("KFSP")
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .exportLimits()
                .documentStatus(DocStatus.EXPORTING, OpenedLimitPage.getInstance())
                .documentStatus(DocStatus.EXPORTED, OpenedLimitPage.getInstance())
                .dateExportToTCExists()
                .sendLimits()
                .documentStatus(DocStatus.FINISHED, OpenedLimitPage.getInstance())
                .dateExportToLKExists()
                .checkOutData(LimUtils.fileToLimitsDto(new LimitSteps().downloadLimFile()),
                        LimUtils.fileToLimitsDto(new File("src/test/resources/Limits/limits out/test3_3.lim")));
    }

    @Test
    @Tag("CI2")
    @Order(0)
    @Link(name = "SPB-T283", url = "https://j.bellintegrator.com/secure/Tests.jspa#/testCase/SPB-T283")
    @DisplayName("SPB-T283. Негативный. Проверка экспорта лимитов в рамках периода до начала дня")
    public void SPB_T283Test(){
        WaitingUtils.waitUntil(LocalDateTime.now().withHour(8).withMinute(0));
        Credentials.setEnv("test");
        File file = new File("src/test/resources/Limits/REPO_SP_LIMITS_11012024_007(ТК_2.2).xml");
        new LimitSteps()
                .openSiteGoToLimitsSection()
                .uploadREPO("Комитет финансов СПб", file)
                .openLimitPage(getLastName("xml"))
                .documentStatus(DocStatus.IN_WORK, OpenedLimitPage.getInstance())
                .exportLimits()
                .timeErrorLate();
    }
}
