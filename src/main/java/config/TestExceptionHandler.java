package config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import utils.DesktopUtils;
import utils.SpvbUtils;

@Slf4j
public class TestExceptionHandler implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        log.error("Uncaught exception", throwable);
        try {
            SpvbUtils.takeNameScreenshot("Fail web screenshot");
        } catch (Exception | Error ignored) {}
        try {
            DesktopUtils.takeNameScreenshot("Fail desktop screenshot");
        } catch (Exception | Error ignored) {}
        throw throwable;
    }
}
