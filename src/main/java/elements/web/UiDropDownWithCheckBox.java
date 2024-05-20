package elements.web;

import com.codeborne.selenide.SelenideElement;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Condition.clickable;
import static com.codeborne.selenide.Selenide.$x;

@AllArgsConstructor
@SuppressWarnings({"UnusedReturnValue"})
public class UiDropDownWithCheckBox {
    public SelenideElement element;
    public String name;

    public UiDropDownWithCheckBox selectOptions(Map<String, Boolean> values) {
        UiTextBox search = new UiTextBox(element, "Поиск в дропдауне " + name);
        for (Map.Entry<String, Boolean> entry : values.entrySet()) {
            search.setValue(entry.getKey());
            SelenideElement checkBoxEl = $x(String.format("//li//*[contains(text(),'%s')]//input", entry.getKey()));
            UiCheckbox checkBox = new UiCheckbox(checkBoxEl, "Чекбокс " + entry.getKey(), checkBoxEl.isSelected());
            checkBox.setCheckbox(entry.getValue());
            search.clear();
        }
        return this;
    }

    public UiDropDownWithCheckBox selectOptions(List<String> options) {
        Map<String, Boolean> temp = new HashMap<>();
        for (String str : options)
            temp.put(str, true);

        selectOptions(temp);
        return this;
    }

    public UiDropDownWithCheckBox clear() {
        SelenideElement clearButton = element.$x("./following-sibling::div//button[@title='Clear']");
        if (clearButton.is(clickable))
            clearButton.click();
        return this;
    }
}
