package page.web.initiators.overCountRepo;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import constants.Initiator;
import elements.web.UiButton;
import elements.web.UiDropdown;
import elements.web.UiTable;
import page.web.initiators.InitiatorsPage;
import utils.SpvbUtils;

import java.io.File;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static elements.columns.OverCountRepoColumn.NAME_DOC;

@SuppressWarnings("UnusedReturnValue")
public class OverCountRepoPage extends InitiatorsPage {

    private final UiButton selectApps = new UiButton($x("//*[text()='Отбор заявок']/ancestor::button"), "Отбор заявок");
    private final UiTable table = new UiTable($x("//table"), "Список заявок");
    public static OverCountRepoPage instance;

    public static OverCountRepoPage getInstance() {
        if (instance == null) {
            instance = new OverCountRepoPage();
        }
        return instance;
    }

    public OverCountRepoPage clearFilters() {
        UiButton button = new UiButton($x("//span[text() = 'Сбросить']"),
                "Кнопка сбросить");
        button.click();
        return this;
    }

    public OverCountRepoPage startSelectApps(Initiator initiator, File file) {
        selectApps.click();
        new UiDropdown($x("//*[text()='Инициатор']/../../../following-sibling::div"), "Инициатор")
                .selectOption(initiator.getInitiatorName());

        SpvbUtils.step("Загрузить файл " + file.getName());
        SelenideElement inputFile = $x("//div[@class = 'MuiDialogContent-root']//input[@type='file']");
        inputFile.uploadFile(file);
        new UiButton($x("//*[text()='Сохранить']/ancestor::button"), "Сохранить").click();

        return this;
    }

    public void waitLimitUpload(String name) {
        table.getCell(name, NAME_DOC).should(Condition.exist, Duration.ofMinutes(10));
    }

    public OverCountRepoPage openAppByNameDoc(String nameDoc) {
        table.getCell(nameDoc, NAME_DOC).click();
        return this;
    }

}
