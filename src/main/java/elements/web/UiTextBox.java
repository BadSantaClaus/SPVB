package elements.web;

import com.codeborne.selenide.SelenideElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.Keys;
import utils.SpvbUtils;

import static com.codeborne.selenide.Selenide.$x;

@Getter
@AllArgsConstructor
public class UiTextBox {

    public SelenideElement element;
    public String name;

    public void setValue(String value) {
        if (!noNeedChange(value)) {
            clear();
            SpvbUtils.step(String.format("Ввести значение \"%s\" в поле \"%s\"", value, name));
            element.scrollIntoView(true).setValue(value);
            return;
        }
        SpvbUtils.step(String.format("Ввести значение \"%s\" в поле \"%s\"", value, name));
    }

    public boolean noNeedChange(String value) {
        return value.equals(getValue());
    }

    public String getValue() {
        return element.getValue();
    }

    public void clear() {
        SpvbUtils.step(String.format("Очистить поле \"%s\"", name));
        element.scrollIntoView(true).setValue(Keys.chord(Keys.CONTROL, "a") + Keys.DELETE);
    }

    public void sendKeys(String keys){
        clear();
        element.sendKeys(keys);
        SpvbUtils.step(String.format("Ввести значение \"%s\" в поле \"%s\"", keys, name));
    }

    public void setValueFromDropDown(String value){
        setValue(value);
        $x("//div[@class='MuiAutocomplete-popper']//li[text()[contains(., '" + value + "')]]").click();
        SpvbUtils.step(String.format("Выбрать из выпадающего списка вариант %s", value));
    }
}
