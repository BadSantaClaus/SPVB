package elements.web;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import elements.columns.BankRussiaProcessColumn;
import elements.columns.IHasColumnDescriptor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class UiTable {

    private static final Logger log = LoggerFactory.getLogger(UiTable.class);
    SelenideElement container;
    String name;

    public ElementsCollection getColumn(IHasColumnDescriptor column) {
        List<String> columnNames = container.$$(By.tagName("th")).texts();
        String cellXpath = String.format(".//td[position()=%s]", columnNames.indexOf(column.getName()) + 1);
        return container.$$x(cellXpath);
    }

    public ElementsCollection getRowElements(String fieldContainsText, IHasColumnDescriptor column) {
        String xPath = column.getXPath();
        if (xPath.endsWith("]")) {
            String subXPath = xPath.substring(0, xPath.lastIndexOf("]"));
            return container.$$x(String.format("%s and contains(text(), '%s')]/ancestor::tr", subXPath, fieldContainsText));
        } else {
            return container.$$x(String.format("%s[contains(text(), '%s')]/ancestor::tr", xPath, fieldContainsText));
        }
    }

    public List<Map<IHasColumnDescriptor, SelenideElement>> getRowMapList(IHasColumnDescriptor searchColumn, IHasColumnDescriptor[] columns, String containsText) {
        List<Map<IHasColumnDescriptor, SelenideElement>> resultList = new ArrayList<>();
        getRowElements(containsText, searchColumn)
                .forEach(row -> {
                    Map<IHasColumnDescriptor, SelenideElement> result = new HashMap<>();
                    for (IHasColumnDescriptor column : columns) {
                        result.put(column, row.$x(String.format(".//%s", column.getXPath())));
                    }
                    resultList.add(result);
                });
        return resultList;
    }

    public SelenideElement getCell(String containsText, IHasColumnDescriptor column) {
        return getRowElements(containsText, column).first().$x(column.getXPath());
    }

    public SelenideElement getCell(String containsText, IHasColumnDescriptor searchColumn, IHasColumnDescriptor targetColumn) {
        return getRowElements(containsText, searchColumn).first().$x(targetColumn.getXPath());
    }

    public SelenideElement getCell(SelenideElement row, IHasColumnDescriptor column) {
        return row.$x(column.getXPath());
    }

    public SelenideElement getProcessChild(String processName, String childName) {
        return getRowElements(processName, BankRussiaProcessColumn.PROCESS_NAME).first()
                .$x(String.format("./following-sibling::tr//*[contains(text(),'%s')]", childName));
    }

    public boolean isTableEmpty() {
        return container.$x(".//*[text()='Нет данных']").exists();
    }
}
