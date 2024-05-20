package elements.web;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;

@AllArgsConstructor
public class UiDatePicker {

    public SelenideElement element;
    public String name;



    public void setRange(LocalDate from, LocalDate to) {
        element.click();
        setDate(from);
        setDate(to);
    }

    private void setDate(LocalDate date){
        setMonth(date);
        setYear(date);
        setDay(date);
    }

    private void setMonth(LocalDate date) {
        String month = date.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
        while (!datePickerHeader().$("div[class*=month]").text().contains(month)){
            nextMonthButton().click();
        }
    }

    private void setYear(LocalDate date){
        String year = String.valueOf(date.getYear());
        SelenideElement yearButton = datePickerHeader().$("div[class*=year]");
        if(yearButton.text().contains(year))
            return;
        yearButton.click();
        ElementsCollection dropDownValues = datePickerHeader().$$("div[class*=year-option]");
        while(!dropDownValues.texts().contains(year)){
            List<Integer> years = dropDownValues.texts().stream()
                    .filter(a->!a.isEmpty())
                    .map(a->a.contains("\n")?a.substring(a.indexOf("\n")+1):a)
                    .map(Integer::parseInt).toList();
            int max = years.stream().collect(Collectors.summarizingInt(Integer::intValue)).getMax();
            int min = years.stream().collect(Collectors.summarizingInt(Integer::intValue)).getMin();
            int curr = Integer.parseInt(year);

            if (max<curr){
              dropDownValues.get(0).click();
            }

            if(min>curr){
                dropDownValues.last().click();
            }
        }
        dropDownValues.filter(Condition.text(year)).first().click();
    }

    private void setDay(LocalDate date){
        int day = date.getDayOfMonth();
        datePicker().$$x(String.format(".//div[contains(@class, 'week')]//*[text()='%s']", day)).first().click();
    }

    private SelenideElement datePicker(){
        return $("div.react-datepicker");
    }

    private SelenideElement datePickerHeader(){
        return datePicker().$("div[class*=header]");
    }

    private SelenideElement nextMonthButton(){
        return datePicker().$("button[aria-label^=Next]");
    }

}
