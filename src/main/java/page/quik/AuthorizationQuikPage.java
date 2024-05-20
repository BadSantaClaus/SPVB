package page.quik;

import FlaNium.WinAPI.DesktopElement;
import FlaNium.WinAPI.elements.Button;
import FlaNium.WinAPI.elements.ComboBox;
import FlaNium.WinAPI.elements.TextBox;
import config.DesktopConfiguration;
import constants.Credentials;
import constants.FilePath;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import utils.WaitingUtils;

import static elements.Components.*;
import static utils.DesktopUtils.*;

@Slf4j
@NoArgsConstructor
public class AuthorizationQuikPage extends BaseQuikPage {
    private ComboBox server() {
        return desktopComboBox(findByAutomationId("10103"), "Сервер");
    }

    public TextBox login() {
        return desktopTextBox(findByAutomationId("10101"), "Логин");
    }

    public TextBox password() {
        return desktopTextBox(findByAutomationId("10102"), "Пароль");
    }

    public Button noUpdateButton() {
        return desktopButton(findByAutomationId("7"), "Не обновлять версию программы");
    }


    public void loginBank(FilePath appPath) {
        login(appPath.getValue(), Credentials.getInstance().bankQuikLogin(), Credentials.getInstance().bankQuikPassword());
    }

    public void loginCb(FilePath appPath) {
        login(appPath.getValue(), Credentials.getInstance().cbQuikLogin(), Credentials.getInstance().cbQuikPassword());
    }

    @SneakyThrows
    public void login(String appPath, String login, String password) {
        DesktopConfiguration.configureDesktopDriver(appPath);

        cancelButton().click();
        showMessageWindow();
        WaitingUtils.sleep(1);
        openLoginWindow();

        if (System.getProperty("env").contains("dev"))
            server().select("192.168.206.32");
        else
            server().select("192.168.206.33");
        login().setText(login);
        password().setText(password);
        okButton().click();

        WaitingUtils.sleep(5);
        checkLastDialogueMessageNotContains("Соединение установить не удалось", "Проверить, что логирование в Quik прошло успешно");

        try {
            noUpdateButton().click();
        } catch (AssertionError e) {
            log.info("Сообщение с предложением обновить версию не появлялось");
        }
        maximizeWindow();
    }

    public void openLoginWindow() {
        new DesktopElement(findByName("Установить соединение с информационным сервером")).click();
    }

}
