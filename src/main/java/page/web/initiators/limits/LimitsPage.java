package page.web.initiators.limits;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import constants.Credentials;
import constants.DocStatus;
import db.initiators.limits.LimitsDbHelper;
import elements.web.UiButton;
import elements.web.UiTextBox;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import page.web.initiators.InitiatorsPage;
import utils.SpvbUtils;

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$x;

@SuppressWarnings("UnusedReturnValue")
public class LimitsPage extends InitiatorsPage {
    SelenideElement uploadREPOButton = $x("//button//span[text()='Загрузить файл лимитов (РЕПО)']");
    SelenideElement uploadMKRButton = $x("//button//span[text()='Загрузить файл лимитов (МКР)']");

    @Step("Выставляем инициатора {initiator}")
    private void setInitiator (String initiator){
        UiTextBox uiTextBox = new UiTextBox($x("//input[ancestor::div[preceding-sibling::div/div/p/label[text()='Инициатор']]and @type='text']"), "Инициатор");
        uiTextBox.setValueFromDropDown(initiator);
    }

    @Step("Прикрепляем файл")
    private void uploadFile (File file){
        SelenideElement inputFile = $x("//div[@class = 'MuiDialogContent-root']//input[@type='file']");
        inputFile.sendKeys(file.getAbsolutePath());
    }

    public LimitsPage uploadREPO (String initiator, File file){
        uploadREPOButton.click();
        setInitiator(initiator);
        uploadFile(file);
        UiButton save = new UiButton($x("//button/span[text()='Сохранить']"), "Сохранить");
        save.click();
        waitLimitsLoad();
        waitLimitUpload(new LimitsDbHelper(constants.Credentials.getInstance().dbUrl(), constants.Credentials.getInstance().dbLogin(),
                Credentials.getInstance().dbPassword()).getLastName(SpvbUtils.getFileExtension(file.getPath())));
        SpvbUtils.takeScreenshot();
        return this;
    }

    public LimitsPage uploadMKR(String initiator, File file){
        uploadMKRButton.click();
        setInitiator(initiator);
        uploadFile(file);
        UiButton save = new UiButton($x("//button/span[text()='Сохранить']"), "Сохранить");
        save.click();
        waitLimitsLoad();
        waitLimitUpload(new LimitsDbHelper(Credentials.getInstance().dbUrl(), Credentials.getInstance().dbLogin(),
                Credentials.getInstance().dbPassword()).getLastName(SpvbUtils.getFileExtension(file.getPath())));
        SpvbUtils.takeScreenshot();
        return this;
    }

    private void waitLimitUpload(String limitName){
        $x("//table//tr/td/button[text()='" + limitName + "']").should(Condition.exist, Duration.ofSeconds(60));
    }

    public List<String> getAllLimitsNames(){
        waitLimitsLoad();
        return WebDriverRunner.getWebDriver()
                .findElements(By.xpath("//table//tr/td[1]"))
                .stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public OpenedLimitPage openLimitPage(String limitName){
        waitLimitsLoad();
        $x("//table//tr/td/button[text()='" + limitName + "']").click();
        waitLimitsLoad();
        SpvbUtils.takeScreenshot();
        return Selenide.page(OpenedLimitPage.class);
    }

    public LimitsPage documentStatus(String name, DocStatus status){
        Assertions.assertThat(status.getValue())
                        .as("Проверить, что статус документа " + status.getValue())
                                .isEqualTo($x("//span[ancestor::tr/td[1]/button[text() = '" + name +"']]").getText());
        return this;
    }

}
