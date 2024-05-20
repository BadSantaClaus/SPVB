package page.web.initiators.auction;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebDriverRunner;
import elements.web.UiButton;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class OpenedAucOrTradeApplicationsPage extends OpenAucOrTradeHeaderPage {

    private static OpenedAucOrTradeApplicationsPage instance;
    public static OpenedAucOrTradeApplicationsPage getInstance() {
        if (instance == null) {
            instance = new OpenedAucOrTradeApplicationsPage();
        }
        return instance;
    }

    public OpenedAucOrTradeApplicationsPage noData(){
        Assertions.assertThat($x("//tr/td[text()='Нет данных']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что в скроллере нет данных по заявкам")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeApplicationsPage sendExists(){
        Assertions.assertThat($x("//span[text()='Отправить']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что отображается кнопка 'Отправить'")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeApplicationsPage send(){
        UiButton button = new UiButton($x("//span[text()='Отправить']"), "Отправить");
        button.click();
        return this;
    }

    public OpenedAucOrTradeApplicationsPage confirmSend(){
        UiButton button = new UiButton($x("//div[@class[contains(., 'MuiDialogActions')]]//span[text()='Отправить']"), "Отправить");
        button.click();
        SpvbUtils.takeScreenshot();
        return this;
    }

    public OpenedAucOrTradeApplicationsPage allFormatsAvailable(){
        Assertions.assertThat($x("//span[text()='DBF']").has(Condition.exist, Duration.ofSeconds(10)) &&
                        $x("//span[text()='PDF']").has(Condition.exist, Duration.ofSeconds(10)) &&
                        $x("//span[text()='DOCX']").has(Condition.exist, Duration.ofSeconds(10)) &&
                        $x("//span[text()='XML']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что доступны все 4 формата реестра")
                .isTrue();
        return this;
    }

    public List<WebElement> getApl(){
        Callable check = () ->
                !$$x("//tbody//tr").isEmpty();
        WaitingUtils.waitUntil(25, 1, 1, "отчёт отображается", check);
        List<WebElement> list = WebDriverRunner.getWebDriver().findElements(By.xpath("//tbody//tr"));
        return list;
    }
}
