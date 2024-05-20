package ru.spvb.steps.overCountRepo;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import config.DesktopConfiguration;
import constants.*;
import db.initiators.overCountRepo.OverCountRepoDbHelper;
import elements.columns.OverCountRepoColumn;
import elements.web.UiButton;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.tree.DefaultAttribute;
import page.quik.AuthorizationQuikPage;
import page.quik.DynamicTransactionPage;
import page.web.AuthorizationWebPage;
import page.web.BasePage;
import page.web.initiators.InitiatorsPage;
import page.web.initiators.auction.FilterInitiators;
import page.web.initiators.overCountRepo.OverCountRepoAppPage;
import page.web.initiators.overCountRepo.OverCountRepoPage;
import page.web.initiators.routineOperations.InitRoutineOperationsPage;
import ru.spvb.steps.FilterSteps;
import ru.spvb.steps.auction.AuctionSteps;
import utils.AllureEdit;
import utils.SftpUtils;
import utils.SpvbUtils;
import utils.XmlUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Selenide.$x;
import static constants.DateFormat.SFTP_DATE_FORMAT2;
import static constants.DateFormat.XML_DATE_FORMAT;
import static constants.DecimalFormats.BIG_DOUBLE;
import static constants.DecimalFormats.BIG_DOUBLE_WITH_SPACE;
import static constants.FilePath.REPORT_USERS_OUT;
import static constants.FilePath.TEMP_FILES;
import static java.time.format.DateTimeFormatter.ofPattern;
import static page.quik.CreateAppPage.FieldNames.RATE_REPO;
import static page.quik.CreateAppPage.FieldNames.SUM_REPO;

@SuppressWarnings("UnusedReturnValue")
public class OverCountRepoSteps {
    private File loTri;
    private File loTro;
    private File loTrr;

    @Step("Открыть страницу 'Внебиржевое РЕПО'")
    public OverCountRepoSteps openPage() {
        if (!BasePage.isLoggedIn())
            AuthorizationWebPage.getInstance().loginUser();
        InitiatorsPage.getInstance().open(MenuTab.INITIATORS, InitiatorsPage.class)
                .openSection(InitiatorsPage.UiInitiatorsSections.OVER_COUNT_REPO, OverCountRepoPage.class);
        $x("//*[contains(text(), 'Нет данных')]").should(disappear, Duration.ofSeconds(10));
        return this;
    }

    @Step("Нажать Отбор заявок и ввести данные")
    public OverCountRepoSteps startSelectAppsProcess(Initiator initiator, String biCode, String filePath) {
        File file = new File(filePath);
        OverCountRepoPage.getInstance().startSelectApps(initiator, file);
        OverCountRepoPage.getInstance().waitLimitsLoad();
        OverCountRepoPage.getInstance().waitLimitUpload(new OverCountRepoDbHelper().getLastName(initiator, biCode));
        return this;
    }

    @Step("Нажать Отбор заявок и ввести данные")
    public OverCountRepoSteps startSelectAppsProcess(Initiator initiator, String filePath) {
        File file = new File(filePath);
        OverCountRepoPage.getInstance().startSelectApps(initiator, file);
        OverCountRepoPage.getInstance().waitLimitsLoad();
        return this;
    }

    @Step("Открыть аукцион")
    public OverCountRepoSteps openAuc(String name) {
        OverCountRepoPage.getInstance().openAppByNameDoc(name);
        return this;
    }

    public OverCountRepoSteps filterByName(String name) {
        new FilterSteps().setFilter(InitiatorsPage.FilterColumns.NAME_DOC, List.of(name));
        return this;
    }

    @Step("Проверить статус аукциона")
    public OverCountRepoSteps checkAucStatus(DocStatus expected) {
        OverCountRepoAppPage.getInstance().checkAucStatus(expected);
        return this;
    }

    @Step("Сгенерировать файл SPCEX_DOC_DS_574_REPOORDERS_YYYYMMDD_0000N.xml")
    @SneakyThrows
    public OverCountRepoSteps generateAndUploadSpceFile(String resultName, String initiatorId, Map<String, String> rateStatus, Map<String, String> changeQuant) {
        File file = $x(String.format("//button[contains(.,'SPCEX_DOC_DS_%s_REPOORDERS_')]", initiatorId)).download();
        Document document = XmlUtils.parseXml(file);
        document.selectSingleNode("//list_of_order").setName("list_of_accept");

        setOrderStatuses((Element) document.selectSingleNode("//list_of_accept"), rateStatus, changeQuant);
        XmlUtils.toXml(document, TEMP_FILES.getValue() + resultName);
        return this;
    }

    @SuppressWarnings("RedundantExplicitVariableType")
    public OverCountRepoSteps setOrderStatuses(Element list, Map<String, String> rateStatus, Map<String, String> changeQuant) {
        List nodes = list.selectNodes("//list_of_order_rec");
        for (Object node : nodes) {
            ((Element) node).setName("list_of_accept_rec");
            String status = rateStatus.get(((Element) node).selectSingleNode("@quant").getText());
            ((Element) node).addAttribute("order_status", status);
            DefaultAttribute quantNode = (DefaultAttribute) ((Element) node).selectSingleNode("@quant");
            if (changeQuant.containsKey(quantNode.getText())) {
                quantNode.setText(changeQuant.get(quantNode.getText()));
            }
        }
        return this;
    }

    @Step("Скачать и проверить файл TRADEREGISTRY")
    @SneakyThrows
    public OverCountRepoSteps checkTradeRegistry(List<String> data) {
        String buttonName = "Скачать реестр отправленных сделок в НРД";
        SpvbUtils.step("Нажать" + buttonName);
        File file = $x(String.format("//button[contains(.,'%s')]", buttonName)).download();
        String str = Files.readString(file.toPath());
        SpvbUtils.step("Проверить, что файл содержит атрибуты [doc_no, doc_date_time, registry]");
        Assertions.assertThat(str)
                .contains("\"doc_no\"")
                .contains("\"doc_date_time\"")
                .contains("\"registry\"");
        String tradergistryTemplate = "\"liability_vol\":\"%s\"";

        for (String attr : data) {
            String jsonAttr = String.format(tradergistryTemplate, new DecimalFormat(BIG_DOUBLE.getValue())
                    .format(Double.parseDouble(attr))).replaceAll(",", ".");
            Assertions.assertThat(str)
                    .contains(jsonAttr)
                    .describedAs(String.format("Проверить, что атрибут [%s] есть в файле", jsonAttr));
        }

        return this;
    }

    @Step("Проверить статус документа внутри аукциона")
    public OverCountRepoSteps checkDocStatus(DocStatus expected) {
        OverCountRepoAppPage.getInstance().checkDocumentStatus(expected);
        return this;
    }

    @Step("Проверить, что кнопка с текстом '{buttonText}' доступна")
    public OverCountRepoSteps checkButtonVisible(String buttonText) {
        UiButton element = OverCountRepoAppPage.getInstance().spanButton(buttonText);
        element.getElement().should(Condition.interactable, Duration.ofSeconds(10));
        return this;
    }

    public OverCountRepoSteps checkTableRowExists(Map<String, String> data) {
        OverCountRepoAppPage.getInstance().checkTableContainsRow(data);
        return this;
    }

    @Step("Проверить таблицу на вкладке 'Реестр заявок'")
    public OverCountRepoSteps checkTableRowAppReg(Map<String, String> appData) {
        checkTableRowExists(Map.of(
                RATE_REPO.getValue(), new DecimalFormat(BIG_DOUBLE.getValue())
                        .format(Double.parseDouble(appData.get(RATE_REPO.getValue())))
                        .replaceAll(",", "."),
                "Сумма РЕПО в валюте расчетов", new DecimalFormat(BIG_DOUBLE_WITH_SPACE.getValue())
                        .format(Double.parseDouble(appData.get(SUM_REPO.getValue())))
                        .replaceAll(",", ".")
                        .replaceAll(" ", " ")
        ));

        return this;
    }


    @Step("Проверить таблицу на вкладке 'Реестр встречных заявок'")
    public OverCountRepoSteps checkTableRowCounterClaimsReg(Map<String, String> appData, String status) {
        checkTableRowExists(Map.of(
                "Ставка РЕПО", new DecimalFormat(BIG_DOUBLE.getValue())
                        .format(Double.parseDouble(appData.get(RATE_REPO.getValue())))
                        .replaceAll(",", "."),
                "Статус заявки", status
        ));
        return this;
    }

    @Step("Проверить таблицу на вкладке 'Реестр сделок'")
    public OverCountRepoSteps checkTableTransactionsReg(Map<String, String> appData) {
        checkTableRowExists(Map.of(
                "Ставка РЕПО", new DecimalFormat(BIG_DOUBLE.getValue())
                        .format(Double.parseDouble(appData.get(RATE_REPO.getValue())))
                        .replaceAll(",", "."),
                "Сумма", new DecimalFormat(BIG_DOUBLE_WITH_SPACE.getValue())
                        .format(Double.parseDouble(appData.get(SUM_REPO.getValue())))
                        .replaceAll(",", ".")
                        .replaceAll(" ", " ")
        ));
        return this;
    }

    public OverCountRepoSteps checkTableNrd(Map<String, String> appData, String status) {
        checkTableRowExists(Map.of(
                "Ставка РЕПО", new DecimalFormat(BIG_DOUBLE.getValue())
                        .format(Double.parseDouble(appData.get(RATE_REPO.getValue())))
                        .replaceAll(",", "."),
                "Статус", status
        ));
        return this;
    }

    @Step("Ввести 'Условия расчетов'")
    public OverCountRepoSteps insertPaymentTerm(String str) {
        OverCountRepoAppPage.getInstance().insertPaymentTerms(str);
        return this;
    }

    @Step("Прикрепить файл к аукциону")
    public OverCountRepoSteps uploadFileInAuc(String filePath) {
        AllureEdit.removeParamByName("filePath");
        File file = new File(filePath);
        OverCountRepoAppPage.getInstance().uploadDoc(file);
        return this;
    }

    @Step("Проверить названия прикрепленных документов")
    public OverCountRepoSteps checkAucDocNames(List<String> expected) {
        OverCountRepoAppPage.getInstance().checkDocNames(expected);
        return this;
    }

    @Step("Экспортировать аукцион в ТС")
    public OverCountRepoSteps exportToTC() {
        OverCountRepoAppPage.getInstance().spanButton("Экспортировать аукцион в ТС").click();
        return this;
    }

    @Step("Нажать кнопку 'Рассылка объявления УТ'")
    public OverCountRepoSteps clickSendUT() {
        OverCountRepoAppPage.getInstance().spanButton("Рассылка объявления УТ").click();
        return this;
    }

    @Step("Нажать кнопку 'Отправить'")
    public OverCountRepoSteps clickSend() {
        OverCountRepoAppPage.getInstance().spanButton("Отправить").click();
        return this;
    }

    @Step("Нажать кнопку 'Отклонить'")
    public OverCountRepoSteps clickReject() {
        OverCountRepoAppPage.getInstance().spanButton("Отклонить").click();
        return this;
    }

    @Step("Нажать кнопку 'Получить статус из НРД'")
    public OverCountRepoSteps clickGetStatusFromNrd() {
        String name = "Получить статус из НРД";
        OverCountRepoAppPage.getInstance().spanButton(name).getElement().should(enabled, Duration.ofMinutes(10));
        OverCountRepoAppPage.getInstance().spanButton(name).click();
        OverCountRepoAppPage.getInstance().spanButton(name).getElement().should(disappear, Duration.ofMinutes(1));
        return this;
    }

    @Step("Скачать ответ НРД и проверить тег result")
    @SneakyThrows
    public OverCountRepoSteps checkNrdAnswer(String expected) {
        File file = OverCountRepoAppPage.getInstance().spanButton("Скачать ответ НРД").download();
        String str = Files.readString(file.toPath());
        String template = "\"result\":\"%s\"";
        Assertions.assertThat(str)
                .contains(String.format(template, expected));
        return this;
    }

    @Step("Нажать кнопку 'Получить'")
    public OverCountRepoSteps clickReceive() {
        OverCountRepoAppPage.getInstance().spanButton("Получить").click();
        return this;
    }

    public OverCountRepoSteps refreshBroker() {
        new AuctionSteps().refreshBroker();
        return this;
    }

    public OverCountRepoSteps openQuik(FilePath appPath) {
        new AuctionSteps().loginQuik(appPath);
        return this;
    }

    public OverCountRepoSteps loadLimFileQuik(FilePath filePath) {
        new AuctionSteps().loadLimFileQuik(filePath);
        return this;
    }

    public OverCountRepoSteps closeQuik() {
        DesktopConfiguration.close();
        return this;
    }

    public OverCountRepoSteps closeBrowser() {
        new AuctionSteps().closeWebDriver();
        return this;
    }

    public OverCountRepoSteps createAppQuik(String instrument, Map<String, String> data) {
        new AuctionSteps().createAppRepo(instrument, data);
        return this;
    }

    public OverCountRepoSteps waitUntil(LocalTime time) {
        new AuctionSteps().waitTime(time);
        return this;
    }

    public OverCountRepoSteps openTab(OverCountRepoAppPage.Tabs tab) {
        OverCountRepoAppPage.getInstance()
                .spanButton(tab.getTabName()).click();
        return this;
    }

    public OverCountRepoSteps checkTableEmpty() {
        OverCountRepoAppPage.getInstance().checkTableEmpty(true);
        return this;
    }

    public OverCountRepoSteps checkTabBlocked(OverCountRepoAppPage.Tabs tab) {
        UiButton tabButton = OverCountRepoAppPage.getInstance().spanButton(tab.getTabName());
        Assertions.assertThat(!tabButton.getElement().isEnabled())
                .isEqualTo(true)
                .describedAs("Проверить, что вкладка заблокирована");
        return this;
    }

    @Step("Проверить наличие файлов на SFTP")
    public OverCountRepoSteps checkResultsInSftp() {
        String resultFileName = $x("//*[contains(text(),'SPCEX_DOC_DS')]").getText();
        String directory = String.format(REPORT_USERS_OUT.getValue() + "%s/XML",
                LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())));
        List<String> files = new SftpUtils().getAllFileNamesInDirectory(directory);
        Assertions.assertThat(files)
                .contains(resultFileName);
        return this;
    }

    @Step("Проверить документы на sftp")
    public OverCountRepoSteps checkReportsSftp(String aucName) {
        SoftAssertions softly = new SoftAssertions();
        SftpUtils sftpUtils = new SftpUtils();
        int idx = Integer.parseInt(aucName.substring(aucName.lastIndexOf("_") + 1, aucName.indexOf(".")));
        String strIdx = String.format("%05d", idx);
        List<String> xmlFiles = sftpUtils.getAllFileNamesInDirectory(
                REPORT_USERS_OUT.getValue() +
                        LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())) + "/XML"
        );
        List<String> docxFiles = sftpUtils.getAllFileNamesInDirectory(
                REPORT_USERS_OUT.getValue() +
                        LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())) + "/DOCX"
        );
        List<String> pdfFiles = sftpUtils.getAllFileNamesInDirectory(
                REPORT_USERS_OUT.getValue() +
                        LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())) + "/PDF"
        );

        String spcex = "SPCEX_DOC_DS_LIST_REPOAUCTIONS_";

        softly.assertThat(xmlFiles.stream().filter(a -> a.matches(spcex + "\\d+_\\d+\\.xml ")).toList())
                .describedAs("Проверить, что нет файлов с маской 'SPCEX_DOC_DS_LIST_REPOAUCTIONS_YYYYMMDD_000NN.xml'")
                .isEmpty();

        softly.assertThat(docxFiles.stream().filter(a -> a.contains(
                        spcex + LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())) + "_" + strIdx + ".docx")))
                .describedAs("Проверить, что есть .doc файл с маской 'SPCEX_DOC_DS_LIST_REPOAUCTIONS_YYYYMMDD_000NN.docx'")
                .isNotEmpty();

        softly.assertThat(pdfFiles.stream().filter(a -> a.contains(
                        spcex + LocalDate.now().format(ofPattern(SFTP_DATE_FORMAT2.getValue())) + "_" + strIdx + ".pdf")))
                .describedAs("Проверить, что есть .doc файл с маской 'SPCEX_DOC_DS_LIST_REPOAUCTIONS_YYYYMMDD_000NN.pdf'")
                .isNotEmpty();

        softly.assertAll();
        return this;
    }

    @Step("Проверить, что процесс завершен на вкладке Регламентные операции")
    public OverCountRepoSteps checkProcessFinishedInRoutineOps(String processName) {
        InitiatorsPage.getInstance()
                .openSection(InitiatorsPage.UiInitiatorsSections.ROUTINE_OPERATIONS, InitRoutineOperationsPage.class)
                .filterProcess(processName)
                .checkLastProcessesFinished();
        return this;
    }

    @Step("Проверить, что процессов нет на вкладке Регламентные операции")
    public OverCountRepoSteps checkProcessNotExistInRoutineOps(String processName) {
        InitiatorsPage.getInstance()
                .openSection(InitiatorsPage.UiInitiatorsSections.ROUTINE_OPERATIONS, InitRoutineOperationsPage.class)
                .filterProcess(processName)
                .checkAllProcessesNotExist();
        return this;
    }

    @Step("Сбросить фильтры")
    public OverCountRepoSteps clearFilter() {
        OverCountRepoPage.getInstance().clearFilters();
        FilterInitiators.getInstance().checkFilterEmpty(InitiatorsPage.FilterColumns.NAME_DOC);
        FilterInitiators.getInstance().checkFilterEmpty(InitiatorsPage.FilterColumns.EXCHANGE_INSTRUMENT);
        FilterInitiators.getInstance().checkFilterEmpty(InitiatorsPage.FilterColumns.DATE);
        FilterInitiators.getInstance().checkFilterEmpty(InitiatorsPage.FilterColumns.STATUS);
        return this;
    }

    @Step("Скачать файл .tri экспорта заявок БР в QUIK")
    public OverCountRepoSteps downloadExtraPlacementLoFile() {
        loTri = OverCountRepoAppPage.getInstance().spanButton("Скачать tri-файл").getElement().download();
        return this;
    }

    @SneakyThrows
    @Step("Подготовить файл LO.tro")
    public OverCountRepoSteps prepareLoTroTrrFile(String name) {
        loTro = new File(TEMP_FILES.getValue() + name + ".tro");
        loTrr = new File(TEMP_FILES.getValue() + name + ".trr");
        if (!loTro.createNewFile()) {
            try (FileWriter fileWriter = new FileWriter(loTro, false)) {
                fileWriter.write("");
            }
        }
        if (!loTrr.createNewFile()) {
            try (FileWriter fileWriter = new FileWriter(loTrr, false)) {
                fileWriter.write("");
            }
        }
        return this;
    }

    @Step("Импортировать транзакции из файлов LO.tri, LO.tro, LO.trr в квик")
    public OverCountRepoSteps processDynamicTransaction(FilePath quikPath) {
        new AuthorizationQuikPage().loginBank(quikPath);
        DynamicTransactionPage page = new DynamicTransactionPage();
        page.openImportTransactionFromFile();
        page.processData(loTri.getAbsolutePath(), loTro.getAbsolutePath(), loTrr.getAbsolutePath());
        closeQuik();
        return this;
    }

    public OverCountRepoSteps fillResultTabFields(String amount, String rate) {
        OverCountRepoAppPage.getInstance().fillResultTabFields(amount, rate);
        clickSend();
        return this;
    }

    public OverCountRepoSteps checkNrdResults(String biCode) {
        LocalDateTime from = LocalDateTime.now().minusDays(14).withHour(0).withMinute(0).withSecond(1);
        LocalDateTime to = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(1);
        FilterSteps filterSteps = new FilterSteps()
                .setFilter(InitiatorsPage.FilterColumns.EXCHANGE_INSTRUMENT, List.of(biCode))
                .setFilter(InitiatorsPage.FilterColumns.DATE, List.of(
                        from.format(ofPattern(XML_DATE_FORMAT.getValue())),
                        to.format(ofPattern(XML_DATE_FORMAT.getValue())))
                ).setFilter(InitiatorsPage.FilterColumns.STATUS, List.of(Status.COMPLETE.getValue()));
        List<String> docs = FilterInitiators.getInstance().getColumnStrings(OverCountRepoColumn.NAME_DOC);
        if(!docs.isEmpty() && docs.get(0).contains("Нет данных"))
            return this;
        Selenide.refresh();

        for (String doc : docs) {
            filterSteps.setFilter(InitiatorsPage.FilterColumns.NAME_DOC, List.of(doc));
            SpvbUtils.step("Проверить статус на вкладке НРД в документе " + doc);
            OverCountRepoPage.getInstance().openAppByNameDoc(doc);
            openTab(OverCountRepoAppPage.Tabs.NRD);
            $x("//*[contains(@class,'MuiBackdrop-root')]").should(enabled)
                    .should(disappear);
            String docStatus = OverCountRepoAppPage.getInstance().docStatus().getText();
            if(docStatus.contains(DocStatus.IN_WORK.getValue())){
                clickGetStatusFromNrd();
            }
            checkDocStatus(DocStatus.FINISHED);
            openPage();
        }
        return this;
    }
}
