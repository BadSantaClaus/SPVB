package page.quik;

import FlaNium.WinAPI.elements.TextBox;
import FlaNium.WinAPI.enums.BasePoint;
import config.DesktopConfiguration;
import org.openqa.selenium.interactions.Actions;
import utils.SpvbUtils;
import utils.WaitingUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openqa.selenium.Keys.*;
import static utils.DesktopUtils.findByAutomationId;
import static utils.DesktopUtils.pressKey;

public class OutStockExchangePage extends BaseQuikPage {

    private final String TEMP_FILE_PATH = "target\\temp\\out_stock.txt";

    private int row;

    public OutStockExchangePage openOutStockExchange() {
        newWindow().click();
        outStockExchange().click();
        okButton().click();
        return this;
    }

    public OutStockExchangePage selectRowByStockCode(String stockCode) {
        row = getStockRow(stockCode) - 1;
        currentTrades().mouseActions().mouseClick(BasePoint.CENTER_TOP, 0, 200);
        new Actions(DesktopConfiguration.driver).keyDown(CONTROL).keyDown(HOME).keyUp(HOME).keyUp(CONTROL).perform();
        WaitingUtils.sleep(1);
        for (int i = 0; i < row; i++) {
            pressKey(DOWN);
        }
        return this;
    }

    public Integer getStockRow(String stockCode) {
        currentTrades().mouseActions().mouseRightClick(BasePoint.CENTER_TOP, 0, 200);
        WaitingUtils.sleep(1);
        for (int i = 0; i <= 8; i++) {
            pressKey(DOWN);
        }
        pressKey(ENTER);
        saveToFile();
        WaitingUtils.sleep(1);
        String table = SpvbUtils.readFromFile(TEMP_FILE_PATH);
        List<String> rows = Arrays.asList(table.split("\\S*\\d{2}:\\d{2}:\\d{2},\\D"));
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < rows.size(); i++) {
            map.put(i, rows.get(i));
        }
        return map.entrySet().stream().filter(m -> m.getValue().contains(stockCode)).map(Map.Entry::getKey).findFirst().orElseThrow();
    }

    public void saveToFile() {
        new TextBox(findByAutomationId("1001", "(@ControlType = 'Edit')")).setText(System.getProperty("user.dir") + "\\" + TEMP_FILE_PATH);
        okButton().click();
    }
}
