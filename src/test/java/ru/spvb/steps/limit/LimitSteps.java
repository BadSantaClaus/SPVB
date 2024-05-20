package ru.spvb.steps.limit;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import constants.Credentials;
import constants.DateFormat;
import constants.DocStatus;
import constants.MenuTab;
import db.initiators.limits.LimitsDbHelper;
import io.qameta.allure.Step;
import lombok.extern.slf4j.Slf4j;
import model.dto.LimitDto;
import model.dto.YTDto;
import org.assertj.core.api.Assertions;
import page.web.AuthorizationWebPage;
import page.web.BasePage;
import page.web.initiators.InitiatorsPage;
import page.web.initiators.limits.LimitsPage;
import page.web.initiators.limits.OpenedLimitPage;
import utils.SftpUtils;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.$x;

@Slf4j
public class LimitSteps {
    public LimitSteps sftpUploadCheck(List<String> before, List<String> after, String code, String type){
        after.removeAll(before);
        boolean found = false;
        Pattern pattern = Pattern.compile("SPCEX_DOC_DS_" + code + "_LIMITS_" +
                LocalDateTime.now().format(DateTimeFormatter
                        .ofPattern(DateFormat.SFTP_DATE_FORMAT2.getValue())) + '_' + "\\d{5}" + "." + type);
        for (String file : after){
            Matcher matcher = pattern.matcher(file);
            found |= matcher.matches();
        }
        Assertions.assertThat(found)
                .as("Проверить, что файл появился в ЛК на sftp")
                .isTrue();
        return new LimitSteps();
    }
    public static String getLastName(String type){
        return new LimitsDbHelper(Credentials.getInstance().dbUrl(), Credentials.getInstance().dbLogin(),
                Credentials.getInstance().dbPassword()).getLastName(type);
    }
    public static String getLastNameDev(String type){
        Credentials.setEnv("dev04");
        return new LimitsDbHelper(Credentials.getInstance().dbUrl(), Credentials.getInstance().dbLogin(),
                Credentials.getInstance().dbPassword()).getLastName(type);
    }

    @Step("Отправить запрос в QExport для проверки кколичества записей")
    public LimitSteps QExportCheck(String tag, List<YTDto> originalData, int count){
        int res = Integer.parseInt(new LimitsDbHelper(Credentials.getInstance().dbArkQExportUrl(),
                Credentials.getInstance().dbArkLogin(),
                Credentials.getInstance().dbArkPassword()).QExportCheck(tag, originalData));
        Assertions.assertThat(res)
                .as("Проверить, что по запросу в QExport count = " + count)
                .isGreaterThanOrEqualTo(count);
        return new LimitSteps();
    }
    @Step("Выставить переключатель лот/рубль в положение {position}")
    public LimitSteps setLotRubleCheckbox(Boolean position){
        OpenedLimitPage.getInstance().setLotRubleCheckbox(position);
        return new LimitSteps();
    }

    @Step("Выставить тег расчётов {calculationTag}")
    public LimitSteps setCalculationsTag(String calculationTag){
        OpenedLimitPage.getInstance().setCalculationsTag(calculationTag);
        return new LimitSteps();
    }

    @Step("Добавить участников торгов")
    public LimitSteps addYTLimits(List<YTDto> list){
        OpenedLimitPage.getInstance().addYTLimits(list);
        return new LimitSteps();
    }

    @Step("Экспортировать лимиты")
    public LimitSteps exportLimits(){
        OpenedLimitPage.getInstance().exportLimits();
        return new LimitSteps();
    }

    @Step("Разослать лимиты")
    public LimitSteps sendLimits(){
        OpenedLimitPage.getInstance().sendLimits();
        return new LimitSteps();
    }

    @Step("Скачать файл лимитов")
    public File downloadLimFile(){
        OpenedLimitPage.getInstance().downloadLimFile();
        return OpenedLimitPage.getInstance().downloadLimFile();
    }

    @Step("Проверить, что документ находится в статусе {status}")
    public LimitSteps documentStatus(DocStatus status, OpenedLimitPage openedLimitPage){
        openedLimitPage.documentStatus(status);
        return new LimitSteps();
    }

    @Step("Проверить, что документ находится в статусе {status}")
    public LimitSteps documentStatus(String name, DocStatus status, LimitsPage limitsPage){
        limitsPage.documentStatus(name, status);
        return new LimitSteps();
    }

    @Step("Проверить, что отображается дата экспорта в ТС")
    public LimitSteps dateExportToTCExists(){
        OpenedLimitPage.getInstance().dateExportToTCExists();
        return new LimitSteps();
    }

    @Step("Проверить, что отображается дата экспорта в ЛК")
    public LimitSteps dateExportToLKExists(){
        OpenedLimitPage.getInstance().dateExportToLKExists();
        return new LimitSteps();
    }

    @Step("Считать данные о лимитах с web")
    public LimitSteps getContent(){
        OpenedLimitPage.getInstance().getContent();
        return new LimitSteps();
    }

    @Step("Загрузить файл лимитов (РЕПО) с инициатором {initiator}")
    public LimitSteps uploadREPO(String initiator, File file){
        new LimitsPage().uploadREPO(initiator, file);
        return new LimitSteps();
    }

    @Step("Загрузить файл лимитов (МКР) с инициатором {initiator}")
    public LimitSteps uploadMKR(String initiator, File file){
        new LimitsPage().uploadMKR(initiator, file);
        return new LimitSteps();
    }

    @Step("Открыть лимит {limitName}")
    public LimitSteps openLimitPage(String limitName){
        new LimitsPage().openLimitPage(limitName);
        return new LimitSteps();
    }

    @Step("Проверить, что данных нет")
    public LimitSteps noContent(){
        $x("//td[text()='Нет данных']").should(Condition.exist);
        return new LimitSteps();
    }

    @Step("Зайти на сайт, авторизоваться и перейти в секцию 'Лимиты'")
    public LimitSteps openSiteGoToLimitsSection(){
        new AuthorizationWebPage()
                .loginUser();
        new BasePage().open(MenuTab.INITIATORS, InitiatorsPage.class);
        new InitiatorsPage().openSection(InitiatorsPage.UiInitiatorsSections.LIMITS, LimitsPage.class);
            return new LimitSteps();
    }

    @Step("Перейтти в секцию 'Лимиты'")
    public LimitSteps navigateToLimitSection(){
        new InitiatorsPage().openSection(InitiatorsPage.UiInitiatorsSections.LIMITS, LimitsPage.class);
        return new LimitSteps();
    }

    @Step("Отклонить документ")
    public LimitSteps decline(){
        OpenedLimitPage.getInstance().decline();
        return new LimitSteps();
    }

    @Step("Возникла ошибка экспорта")
    public LimitSteps exportError(){
        OpenedLimitPage.getInstance().exportError();
        return new LimitSteps();
    }

    public static List<YTDto>  getWebData(){
        ElementsCollection web = OpenedLimitPage.getInstance().getContent();
        List<YTDto> list = new ArrayList<>();
        for (int j = 1; j < web.size();){
            String name = web.get(j).$x(".//td[1]").getText();
            String code = web.get(j).$x(".//td[2]").getText();
            String generalLimit = web.get(j).$x(".//td[5]").getText().replace(" ", "");
            List<List<String>> termLimits = new ArrayList<>();
            j++;
            while (!web.get(j).$x("./td[1]//div[text()]").exists() && j < web.size()){
                List<String> termList = new ArrayList<>();
                termList.add(web.get(j).$x(".//td[4]").getText().replaceAll(" ", ""));
                termList.add(web.get(j).$x(".//td[5]").getText().replaceAll(" ", ""));
                termLimits.add(termList);
                j++;
            }
            list.add(new YTDto(name, code, generalLimit, termLimits));
        }
        return list;
    }

    @Step("Проверить, что данные на странице соответствуют исходным")
    public LimitSteps checkWebData(List<YTDto> web, List<YTDto> required){
        Assertions.assertThat(web)
                .as("Проверить, что данные на странице соответствуют исходным")
                .hasSameElementsAs(required);
        return new LimitSteps();
    }

    public LimitSteps setInstrumentSpec(String spec){
        OpenedLimitPage.getInstance().setInstrumentSpec(spec);
        return new LimitSteps();
    }

    @Step("Проверить, что выходные данные в файле lim соответствуют исходным")
    public LimitSteps checkOutData(List<LimitDto> out, List<LimitDto> required){
        Assertions.assertThat(out)
                .as("Проверить, что выходные данные соответствуют исходным")
                .hasSameElementsAs(required);
        return new LimitSteps();
    }

    @Step("Загрузить файл {file} на SFTP в папку {remoteDir}")
    public LimitSteps uploadSftp(File file, String remoteDir){
        SftpUtils.uploadFile(Path.of(file.toURI()), remoteDir);
        return new LimitSteps();
    }

    public LimitSteps timeErrorLate() {
        OpenedLimitPage.getInstance().timeError();
        return new LimitSteps();
    }
}
