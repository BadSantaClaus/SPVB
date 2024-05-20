package page.web.initiators.auction;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import elements.web.UiButton;
import elements.web.UiTextBox;
import org.assertj.core.api.Assertions;
import utils.SpvbUtils;

import java.io.File;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class OpenedAurOrTradeResultsPage extends OpenAucOrTradeHeaderPage {
    private static OpenedAurOrTradeResultsPage instance;

    public static OpenedAurOrTradeResultsPage getInstance() {
        if (instance == null) {
            instance = new OpenedAurOrTradeResultsPage();
        }
        return instance;
    }

    SelenideElement uploadFile;

    public OpenedAurOrTradeResultsPage uploadExists() {
        if ($x("//input[ancestor::span and @value ='UPLOAD_FILE']").has(Condition.exist, Duration.ofSeconds(10))){
            $x("//input[ancestor::span and @value ='UPLOAD_FILE']").click();
        }
        Assertions.assertThat($x("//input[following-sibling::p[text()='Загрузить файл']]").has(Condition.exist, Duration.ofSeconds(20)))
                .as("Проверить, что отображается кнопка 'Загрузить файл'")
                .isTrue();
        return this;
    }

    public OpenedAurOrTradeResultsPage uploadFile(File file) {
        uploadFile = $x("//input[following-sibling::p[text()='Загрузить файл']]");
        uploadFile.sendKeys(file.getAbsolutePath());
        return this;
    }

    public OpenedAurOrTradeResultsPage invalidAucReasonAreaUnlocked() {
        SelenideElement element = $x("//textarea[@name='deposit_auction_invalid_reason']");
        element.has(Condition.attribute("disabled"), Duration.ofSeconds(5));
        Assertions.assertThat(element.has(Condition.attribute("disabled"), Duration.ofSeconds(5)))
                .as("Проверить, что поле 'Причина признания депозитного аукциона несостоявшимся' доступно")
                .isFalse();
        return this;
    }

    public OpenedAurOrTradeResultsPage fillInvalidAucReason(String reason) {
        SelenideElement element = $x("//textarea[@name='deposit_auction_invalid_reason']");
        element.setValue(reason);
        return this;
    }

    public OpenedAurOrTradeResultsPage fillResUrl(String value) {
        UiTextBox textBox = new UiTextBox($x("//input[@name='result_url']"), "Ссылка на итоги аукцциона");
        textBox.getElement().shouldBe(Condition.visible);
        textBox.setValue(value);
        return this;
    }

    public OpenedAurOrTradeResultsPage decline() {
        UiButton button = new UiButton($x("//button/span[text()='Отклонить']"),
                "Отклонить");
        button.click();
        return this;
    }

    public OpenedAurOrTradeResultsPage save() {
        UiButton button = new UiButton($x("//span[text()='Сохранить']"), "Сохранить");
        button.click();
        return this;
    }

    public OpenedAurOrTradeResultsPage publish() {
        UiButton button = new UiButton($x("//span[text()='Опубликовать на сайте']"), "Опубликовать на сайте");
        button.click();
        return this;
    }

    public OpenedAurOrTradeResultsPage fillAverageRate(String volume) {
        UiTextBox textBox = new UiTextBox($x("//label[contains(text(), 'Средневзвешенная')]/following::input"),
                "Средневзвешенная ставка депозита по удовлетворенными заявкам");
        textBox.setValue(volume);
        return this;
    }

    public OpenedAurOrTradeResultsPage fillRealVolume(String averageRate) {
        UiTextBox textBox = new UiTextBox($x("//label[contains(text(), 'Фактический объем размещения')]/following::input"),
                "Ссылка на итоги аукциона");
        textBox.setValue(averageRate);
        return this;
    }

    public OpenedAurOrTradeResultsPage checkRealVolumeAccess() {
        SelenideElement el = $x("//label[contains(text(), " +
                "'Фактический объем размещения')]/following::input");
        Assertions.assertThat(el.has(Condition.attribute("disabled"), Duration.ofSeconds(5)))
                .as("Проверить, что поле 'Фактический объем размещения средств на текущий процентный период' доступно")
                .isFalse();
        return this;
    }

    public OpenedAurOrTradeResultsPage checkAverageRateAccess() {
        SelenideElement el = $x("//label[contains(text(), " +
                "'Средневзвешенная')]/following::input");
        Assertions.assertThat(el.has(Condition.attribute("disabled"), Duration.ofSeconds(5)))
                .as("Проверить, что поле 'Средневзвешенная ставка депозита по удовлетворенными заявкам' доступно")
                .isFalse();
        return this;
    }

    public OpenedAurOrTradeResultsPage send(){
        UiButton button = new UiButton($x("//span[text()='Отправить']"), "Отправить");
        button.click();
        SpvbUtils.takeScreenshot();
        return this;
    }
}
