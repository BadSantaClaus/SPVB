package elements;

import FlaNium.WinAPI.elements.ListBoxItem;
import com.codeborne.selenide.SelenideElement;
import elements.desktop.*;
import elements.web.UiButton;
import elements.web.UiTable;
import elements.web.UiTextBox;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class Components {

    public static DesktopComboBox desktopComboBox(WebElement element, String name) {
        return new DesktopComboBox(element, name);
    }

    public static DesktopTextBox desktopTextBox(WebElement element, String name) {
        return new DesktopTextBox(element, name);
    }

    public static DesktopTextBox desktopTextBox(WebElement parent, String child, String name) {
        return new DesktopTextBox(parent.findElement(By.xpath(child)), name);
    }

    public static DesktopButton desktopButton(WebElement element, String name) {
        return new DesktopButton(element, name);
    }

    public static DeskTopLabel desktopLabel(WebElement element, String name) {
        return new DeskTopLabel(element, name);
    }


    public static DesktopButton desktopButton(WebElement parent, String xPath, String name) {
        return new DesktopButton(parent.findElement(By.xpath(xPath)), name);
    }

    public static ListBoxItem listBoxItem(WebElement element, String name) {
        return new DesktopListBoxItem(element, name);
    }
    public static DesktopSpinner desktopSpinner(WebElement element, String name){
        return new DesktopSpinner(element, name);
    }
    public static ListBoxItem listBoxItem(WebElement parent, String xPath, String name) {
        return new DesktopListBoxItem(parent.findElement(By.xpath(xPath)), name);
    }

    public static UiTextBox textBox(SelenideElement element, String name) {
        return new UiTextBox(element, name);
    }

    public static UiButton button(SelenideElement element, String name) {
        return new UiButton(element, name);
    }

    public static UiTable table(SelenideElement element, String name) {
        return new UiTable(element, name);
    }


}
