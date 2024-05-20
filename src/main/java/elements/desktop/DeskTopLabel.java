package elements.desktop;

import FlaNium.WinAPI.elements.Label;
import org.openqa.selenium.WebElement;
import utils.SpvbUtils;

public class DeskTopLabel extends Label {

    private final String elementName;

    public DeskTopLabel(WebElement element, String elementName) {
        super(element);
        this.elementName = elementName;
    }

    @Override
    public String getName() {
        SpvbUtils.step(String.format("Получить значение из поля %s", elementName));
        return super.getName();
    }
}
