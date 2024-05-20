package elements.desktop;

import FlaNium.WinAPI.elements.TextBox;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;
import utils.SpvbUtils;

@Slf4j
public class DesktopTextBox extends TextBox {
    private final String name;

    public DesktopTextBox(WebElement element, String name) {
        super(element);
        this.name = name;
    }

    @Override
    public void setText(String text) {
        SpvbUtils.step(String.format("Ввести текст \"%s\" в поле \"%s\"", text, name));
        super.setText(text);
    }

    @Override
    public void click() {
        SpvbUtils.step(String.format("Кликнуть по полю \"%s\"", name));
        super.click();
    }

    @Override
    public String getName() {
        return name;
    }
}
