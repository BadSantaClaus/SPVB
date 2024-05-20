package page.quik;

import FlaNium.WinAPI.elements.Button;
import FlaNium.WinAPI.elements.Label;
import FlaNium.WinAPI.elements.TextBox;
import org.assertj.core.api.Assertions;
import utils.DesktopUtils;
import utils.WaitingUtils;

import static elements.Components.*;
import static utils.DesktopUtils.findByAutomationId;

public class DynamicTransactionPage extends BaseQuikPage {

    public Button initialData() {
        return desktopButton(findByAutomationId("30401"), "Файл с исходными данными о транзакциях");
    }

    public Button sentData() {
        return desktopButton(findByAutomationId("30402"), "Файл с данными об успешно отправленных транзакциях");
    }

    public Button logSentData(){
        return desktopButton(findByAutomationId("30403"), "Файл с журналом отправляеммых транзакций");
    }

    public TextBox pathData() {
        return desktopTextBox(findByAutomationId("1148", "(@ControlType = 'Edit')"), "Путь к файлу");
    }

    public Button startProcessing() {
        return desktopButton(findByAutomationId("30407"), "Начать обработку");
    }

    public Button stopProcessing() {
        return desktopButton(findByAutomationId("30408"), "Прекратить  обработку");
    }

    public Label numberOfTransactionsComplete() {
        return desktopLabel(findByAutomationId("30419"), "Всего выполнено транзакций");
    }

    public void setInitialData(String path) {
        initialData().click();
        pathData().setText(path);
        okButton().click();
    }

    public void setSentData(String path) {
        sentData().click();
        pathData().setText(path);
        okButton().click();
    }

    public void setLogSentData(String path) {
        logSentData().click();
        pathData().setText(path);
        okButton().click();
    }

    public void processData(String initialDataPath, String sentDataPath) {
        setInitialData(initialDataPath);
        setSentData(sentDataPath);
        startProcessing().click();
        WaitingUtils.sleep(3);
        stopProcessing().click();
        DesktopUtils.takeScreenshot();
    }

    public void processData(String initialDataPath, String sentDataPath, String logSentDataPath){
        setInitialData(initialDataPath);
        setSentData(sentDataPath);
        setLogSentData(logSentDataPath);
        startProcessing().click();
        WaitingUtils.sleep(3);
        stopProcessing().click();
        DesktopUtils.takeScreenshot();
    }

    public void checkNumberOfTransactionsComplete(int expected) {
        int actual = Integer.parseInt(numberOfTransactionsComplete().getName());
        DesktopUtils.takeScreenshot();
        Assertions.assertThat(actual)
                .describedAs(String.format("Проверить, что число выполненных транзакций равно %d", expected))
                .isEqualTo(expected);
    }

}
