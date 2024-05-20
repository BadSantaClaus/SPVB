package page.web.initiators.auction;

import com.codeborne.selenide.Condition;
import elements.web.UiButton;
import org.assertj.core.api.Assertions;
import utils.SpvbUtils;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class OpenedAucOrTradeTradesPage extends OpenAucOrTradeHeaderPage{
    private static OpenedAucOrTradeTradesPage instance;
    public static OpenedAucOrTradeTradesPage getInstance() {
        if (instance == null) {
            instance = new OpenedAucOrTradeTradesPage();
        }
        return instance;
    }

    UiButton getButton = new UiButton($x("//span[text()='Получить']"), "Получить");

    public OpenedAucOrTradeTradesPage get(){
        getButton.click();
        return this;
    }

    public OpenedAucOrTradeTradesPage noData(){
        Assertions.assertThat($x("//tr/td[text()='Нет данных']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что сделок нет")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeTradesPage sendExists(){
        $x("//span[text()='Отправить']").should(Condition.exist, Duration.ofSeconds(10));
        return this;
    }

    public OpenedAucOrTradeTradesPage send(){
        UiButton button = new UiButton($x("//span[text()='Отправить']"), "Отправить");
        button.click();
        return this;
    }

    public OpenedAucOrTradeTradesPage confirmSend(){
        UiButton button = new UiButton($x("//div[@class[contains(., 'MuiDialogActions')]]//span[text()='Отправить']"), "Отправить");
        button.click();
        SpvbUtils.takeScreenshot();
        return this;
    }

    public OpenedAucOrTradeTradesPage allFormatsAvailable(){
        Assertions.assertThat($x("//span[text()='DBF']").has(Condition.exist, Duration.ofSeconds(10)) &&
                        $x("//span[text()='PDF']").has(Condition.exist, Duration.ofSeconds(10)) &&
                        $x("//span[text()='DOCX']").has(Condition.exist, Duration.ofSeconds(10)) &&
                        $x("//span[text()='XML']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что доступны все 4 формата реестра")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeTradesPage getChangedToUpdate(){
        Assertions.assertThat($x("//span[text()='Получить']").has(Condition.not(Condition.exist), Duration.ofSeconds(10)))
                .as("Проверить, что кнопка 'Получить' пропала")
                .isTrue();
        Assertions.assertThat($x("//span[text()='Обновить']").has(Condition.exist, Duration.ofSeconds(10)))
                .as("Проверить, что кнопка 'Обновить' появилась")
                .isTrue();
        return this;
    }

    public OpenedAucOrTradeTradesPage update(){
        $x("//span[text()='Обновить']").click();
        return this;
    }
}
