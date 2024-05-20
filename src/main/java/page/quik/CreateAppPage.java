package page.quik;

import FlaNium.WinAPI.elements.Button;
import FlaNium.WinAPI.elements.ComboBox;
import FlaNium.WinAPI.elements.Spinner;
import FlaNium.WinAPI.elements.TextBox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import utils.DesktopUtils;

import java.util.Map;

import static elements.Components.*;
import static page.quik.CreateAppPage.FieldNames.*;
import static utils.DesktopUtils.findByAutomationId;
import static utils.DesktopUtils.findByName;

public class CreateAppPage extends BaseQuikPage{

    @AllArgsConstructor
    @Getter
    public enum FieldNames{
        PARTNER("Партнер"),
        BUY_SELL("Купить/Продать"),
        SUM_REPO("Сумма РЕПО"),
        LOTS("Лотов"),
        CODE_CALC("Код расчетов"),
        TERM_REPO("Срок РЕПО"),
        RATE_REPO("Ставка РЕПО"),
        CODE_CLIENT("Код клиента");
        private final String value;
    }

    public ComboBox partner() {
        return desktopComboBox(findByAutomationId("9573"), PARTNER.getValue());
    }

    public Button buySell() {
        return desktopButton(findByAutomationId("9506"), BUY_SELL.getValue());
    }

    public Spinner sumRepo() {
        return desktopSpinner(findByAutomationId("10822"), SUM_REPO.getValue());
    }

    public Spinner lots() {
        return desktopSpinner(findByAutomationId("9948"), LOTS.getValue());
    }

    public ComboBox codeCalc() {
        return desktopComboBox(findByAutomationId("9581"), CODE_CALC.getValue());
    }

    public Spinner termRepo(){return desktopSpinner(findByAutomationId("10809"), TERM_REPO.getValue());}

    public TextBox rateRepo() {
        return desktopTextBox(findByAutomationId("9587"), RATE_REPO.getValue());
    }

    public ComboBox codeClient() {
        return desktopComboBox(findByAutomationId("9522"), CODE_CLIENT.getValue());
    }

    public Button attentionOk() {
        return desktopButton(findByName("OK"), "OK");
    }

    public CreateAppPage createApp(Map<String, String> data) {
        partner().select(data.getOrDefault(partner().getName(), "Центральный банк РФ [9]"));
        if (data.containsKey(buySell().getName()))
            buySell().click();
        sumRepo().setValue(Double.parseDouble(data.getOrDefault(sumRepo().getName(), String.valueOf(2 * 1000 * 1000))));
        lots().setValue(Double.parseDouble(data.getOrDefault(lots().getName(), "0")));
        codeCalc().select(data.getOrDefault(codeCalc().getName(), "B0"));
        if(data.containsKey(termRepo().getName()))
            termRepo().setValue(Double.parseDouble(data.get(termRepo().getName())));
        rateRepo().setText(data.getOrDefault(rateRepo().getName(), "15"));
        codeClient().select(data.getOrDefault(codeClient().getName(), "0472CAT00001"));
        DesktopUtils.takeNameScreenshot("App data");
        okButton().click();
        attentionOk().click();
        DesktopUtils.takeNameScreenshot("App create message");
        return this;
    }
}
