package elements.desktop;

import FlaNium.WinAPI.elements.Button;
import FlaNium.WinAPI.elements.ComboBox;
import FlaNium.WinAPI.elements.ComboBoxItem;
import FlaNium.WinAPI.elements.TextBox;
import FlaNium.WinAPI.enums.BasePoint;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import utils.SpvbUtils;

import static FlaNium.WinAPI.elements.enums.ExpandCollapseState.COLLAPSED;
import static utils.DesktopUtils.pressKey;


public class DesktopComboBox extends ComboBox {
    private final String name;
    private final Button expandButton;

    public DesktopComboBox(WebElement element, String name) {
        super(element);
        this.name = name;
        expandButton = new Button(super.findElement(By.xpath("//Button")));
    }

    @Override
    public ComboBoxItem select(String value) {
        SpvbUtils.step(String.format("Выбрать значение %s в %s", value, name));
        chooseFirst();
        while (!isValueChosen(value)) {
            if (super.expandCollapseState().equals(COLLAPSED))
                expand();
            if (isEditable())
                new TextBox(chosenValue()).setText(value);
            else
                pressKey(Keys.DOWN);
        }
        collapse();
        return new ComboBoxItem(chosenValue());
    }

    private void chooseFirst() {
        expand();
        expandButton.mouseActions().mouseClick(BasePoint.BOTTOM_LEFT, 0, 5);
        super.collapse();
    }

    private boolean isValueChosen(String value) {
        return chosenValue().getText().contains(value);
    }

    private WebElement chosenValue() {
        if (isEditable())
            return super.findElement(By.xpath("//Edit"));
        else
            return super.findElement(By.xpath("//Text"));
    }

    @Override
    public String getName(){
        return name;
    }

}
