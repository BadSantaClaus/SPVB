package page.web;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.ex.UIAssertionError;
import constants.Credentials;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Selenide.*;
import static java.time.Duration.ofSeconds;

public class AuthorizationWebPage {

    public static AuthorizationWebPage instance;

    public static AuthorizationWebPage getInstance() {
        if (instance == null) {
            instance = new AuthorizationWebPage();
        }
        return instance;
    }

    public BankRussiaPage loginUser() {
        for (int i = 0; i < 5; i++) {
            try {
                open("/login");
                WebDriverRunner.getWebDriver().manage().window().maximize();
                $(By.name("username")).setValue(Credentials.getInstance().webLoginUser());
                $(By.name("password")).setValue(Credentials.getInstance().webPasswordUser());
                $x("//span[text()='Войти']/ancestor::button").click();
                $x("//span[text()='Войти']/ancestor::button").should(disappear, ofSeconds(30));
                break;
            } catch (UIAssertionError | TimeoutException e) {
                if (i > 3)
                    throw e;
            }
        }

        return new BankRussiaPage();
    }

}
