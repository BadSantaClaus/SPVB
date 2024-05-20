package page.web.initiators.auction;

import com.codeborne.selenide.Condition;
import elements.web.UiButton;
import org.assertj.core.api.Assertions;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class OpenedAucOrTradeNRDPage extends  OpenAucOrTradeHeaderPage{

    private static OpenedAucOrTradeNRDPage instance;
    public static OpenedAucOrTradeNRDPage getInstance() {
        if (instance == null) {
            instance = new OpenedAucOrTradeNRDPage();
        }
        return instance;
    }
    UiButton getStatusButton = new UiButton($x("//button[child::span[text()='Получить статус из НРД']]"),
            "Получить статус из НРД");

    public UiButton downloadNrdSentRegistry = new UiButton($x("//button[contains(.,'Скачать реестр отправленных сделок в НРД')]"),
                "Скачать реестр отправленных сделок в НРД");

    public OpenedAucOrTradeNRDPage getStatus(){
        getStatusButton.click();
        return this;
    }

    public OpenedAucOrTradeNRDPage checkDownloadExists(){
        Assertions.assertThat($x("//button[child::span[text()='Скачать ответ НРД']]")
                        .has(Condition.exist, Duration.ofSeconds(25)))
                .as("Проверить, что появилась кнопка 'Скачать ответ НРД'")
                .isTrue();
        return this;
    }
}
