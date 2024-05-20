package page.web;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import constants.DateFormat;
import constants.MenuTab;
import elements.web.UiButton;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.NoSuchSessionException;
import utils.SpvbUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;
import static elements.Components.button;

@Slf4j
@SuppressWarnings("UnusedReturnValue")
public class BasePage {

    private final UiButton closeNotifications = button($x("//span[text() = 'Отметить все как прочитанное']//following::button"), "Закрыть уведомления");

    private final UiButton notifications = button($x("//button"), "Открыть уведомления");


    public <T extends BasePage> T open(MenuTab tab, Class<T> nextPageClass) {
        SelenideElement menu = $x(String.format("//span[text()='%s']", tab.getValue()));
        menu.scrollIntoView(true).click();
        SpvbUtils.step(String.format("Открыть вкладку \"%s\"", tab.getValue()));
        return nextPageClass.cast(Selenide.page(nextPageClass));
    }

    public void open(MenuTab tab) {
        SelenideElement menu = $x(String.format("//span[text()='%s']", tab.getValue()));
        menu.scrollIntoView(true).click();
        SpvbUtils.step(String.format("Открыть вкладку \"%s\"", tab.getValue()));
    }

    public void closeWebDriver() {
        Selenide.closeWebDriver();
    }

    public static boolean isLoggedIn() {
        try {
            return !Selenide.webdriver().driver().url().contains("login");
        } catch (NoSuchSessionException | IllegalStateException e) {
            return false;
        }
    }

    public BasePage checkNotificationMessage(String message, LocalDateTime startTime) {
        notifications.click();
        $$x("//h6").should(CollectionCondition.sizeGreaterThan(2));
        List<String> actualList = $$x("//h6").texts();
        List<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < actualList.size(); i++) {
            if (actualList.get(i).contains(startTime.format(DateTimeFormatter.ofPattern(DateFormat.WEB_DATE_TIME_FORMAT_NO_MINUTES.getValue())))) {
                indexes.add(i + 1);
            }
        }

        List<String> actualMessages = new ArrayList<>();
        indexes.forEach(i -> actualMessages.add(actualList.get(i)));
        Assertions.assertThat(actualMessages)
                .describedAs(String.format("Проверить, что уведомления содержат сообщение '%s'", message))
                .contains(message);
        SpvbUtils.takeScreenshot();
        return this;
    }

    public BasePage closeNotifications() {
        closeNotifications.click();
        return this;
    }
}
