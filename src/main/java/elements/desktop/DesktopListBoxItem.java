package elements.desktop;

import FlaNium.WinAPI.elements.ListBoxItem;
import org.openqa.selenium.WebElement;
import utils.SpvbUtils;

public class DesktopListBoxItem extends ListBoxItem {

    private final String name;

    public DesktopListBoxItem(WebElement element, String name) {
        super(element);
        this.name = name;
    }

    @Override
    public void click() {
        SpvbUtils.step(String.format("Выбрать элемент \"%s\" в списке", name));
        super.click();
    }
}
