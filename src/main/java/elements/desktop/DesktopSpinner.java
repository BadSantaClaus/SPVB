package elements.desktop;

import FlaNium.WinAPI.elements.Spinner;
import org.openqa.selenium.WebElement;
import utils.SpvbUtils;

public class DesktopSpinner extends Spinner {

    private final String name;
    public DesktopSpinner(WebElement element, String name) {
        super(element);
        this.name = name;
    }

    @Override
    public void setValue(double value) {
        SpvbUtils.step(String.format("Ввести %s в %s", value, name));
        super.setValue(value);
    }

    @Override
    public String getName(){
        return name;
    }
}
