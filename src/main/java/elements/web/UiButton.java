package elements.web;

import com.codeborne.selenide.SelenideElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import utils.SpvbUtils;

import java.io.File;

@Getter
@AllArgsConstructor
public class UiButton {

    private SelenideElement element;
    private String name;

    public void click() {
        SpvbUtils.step(String.format("Кликнуть по кнопке %s", name));
        element.click();
    }

    public File download() {
        return element.download();
    }

    public void scrollIntoView() {
        element.scrollIntoView(false);
    }

}
