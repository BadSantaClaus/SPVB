package utils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.NoSuchElementException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static constants.DateFormat.XML_TIME_FORMAT;

@Slf4j
public class WaitingUtils {

    @SneakyThrows
    public static void sleep(int seconds) {
        TimeUnit.SECONDS.sleep(seconds);
    }

    /**
     * @param timeout      - максимальное время ожидания в сек
     * @param pollInterval - интервал проверки условия в сек
     * @param initialDelay - качальный интервал ожидания, после которого начинается проверка условия в сек
     * @param action       - условие выхода из ожидания
     */
    public static void waitUntil(int timeout, int pollInterval, int initialDelay, String alias, Callable<Boolean> action) {
        try {
            getConditionFactory(timeout, pollInterval, initialDelay, alias)
                    .until(action);
        } catch (ConditionTimeoutException e) {
            throw new AssertionError(alias, e);
        }
    }

    public static void waitReady(String elementName, Runnable findElement) {
        waitUntil(20, 1, 1, String.format("Ожидание появления элемента \"%s\"", elementName), () -> {
            try {
                findElement.run();
                return true;
            } catch (NoSuchElementException e) {
                return false;
            }
        });
    }

    public static ConditionFactory getConditionFactory(int timeout, int pollInterval, int initialDelay, String alias) {
        return Awaitility.
                await(alias)
                .atMost(timeout, TimeUnit.SECONDS)
                .pollDelay(initialDelay, TimeUnit.SECONDS)
                .pollInterval(pollInterval, TimeUnit.SECONDS)
                .pollInSameThread();
    }

    public static void waitUntil(LocalDateTime releaseTime) {
        if (releaseTime.isBefore(LocalDateTime.now())) {
            return;
        }
        Duration duration = Duration.between(LocalDateTime.now(), releaseTime);
        log.info(String.format("Заданное продолжения теста  - %s. Время ожидания в минутах - %d",
                releaseTime.format(DateTimeFormatter.ofPattern(XML_TIME_FORMAT.getValue())),
                duration.toMinutes()));
        sleep(Math.toIntExact(duration.toSeconds()));
    }

}
