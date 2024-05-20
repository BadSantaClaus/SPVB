package elements.desktop;

import FlaNium.WinAPI.elements.Button;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import utils.SpvbUtils;

@Slf4j
public class DesktopButton extends Button {
    private final String name;

    public DesktopButton(WebElement element, String name) {
        super(element);
        this.name = name;
    }

    @Override
    public void click() {
        SpvbUtils.step(String.format("Кликнуть по кнопке \"%s\"", name));
        super.click();
    }
}
