package elements.web;

import com.codeborne.selenide.SelenideElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import utils.SpvbUtils;

import static com.codeborne.selenide.Selenide.$x;

@Getter
@AllArgsConstructor
public class UiDropdown {

    public SelenideElement element;
    public String name;

    public void selectOption(String option){
        element.click();
        $x("//div[@class='MuiAutocomplete-popper']//li[text()[contains(., '" + option + "')]]").click();
        SpvbUtils.step(String.format("Выбрать из выпадающего списка вариант %s", option));
    }
}
