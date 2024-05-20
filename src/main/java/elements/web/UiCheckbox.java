package elements.web;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import utils.SpvbUtils;

@Getter
@AllArgsConstructor
public class UiCheckbox {
    public SelenideElement element;
    public String name;
    public boolean position;

    public void setCheckbox(Boolean position) {
        SpvbUtils.step(String.format("Поставить переключатель %s в положение %b", name, position));
        if (this.position != position) {
            element.click();
            this.position = position;
        }
    }

    public UiCheckbox(SelenideElement element, String name) {
        this.name = name;
        this.element = element;
    }

    public void setValue(boolean value) {
        SpvbUtils.step(String.format("Поставить переключатель %s в положение %b", name, value));
        if (value) {
            if (!element.has(Condition.attribute("checked"))) {
                element.click();
            }
        } else {
            if (element.has(Condition.attribute("checked"))) {
                element.click();
            }
        }
    }

}
