package page.quik;

import elements.desktop.DesktopTextBox;
import org.openqa.selenium.By;
import utils.DesktopUtils;

import static elements.Components.desktopTextBox;
import static elements.Components.listBoxItem;
import static org.openqa.selenium.Keys.F7;
import static utils.DesktopUtils.findByClassName;
import static utils.DesktopUtils.findByName;

@SuppressWarnings({"UnusedReturnValue"})
public class NewWindowPage extends BaseQuikPage {

    private DesktopTextBox search() {
        return desktopTextBox(
                findByClassName("SearchTreeCtrlClass").findElement(By.xpath("//Edit")),
                "Поиск окна");
    }

    public NewWindowPage searchWindow(String windowName) {
        for (int i = 0; i < 5; i++) {
            try{
                DesktopUtils.pressKey(F7);
                search().setText(windowName);
                break;
            }catch (Exception | Error e){
                if(i>3)
                    throw e;
            }
        }
        return this;
    }

    public NewWindowPage chooseWindow(String windowName) {
        listBoxItem(findByName(windowName), windowName).click();
        okButton().click();
        return this;
    }
}
