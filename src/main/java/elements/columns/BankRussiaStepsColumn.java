package elements.columns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BankRussiaStepsColumn implements IHasColumnDescriptor {

    NUMBER("Порядковый номер шага", ".//td[position() = 1]"),
    STEP_NAME("Название шага", ".//td[position() = 2]"),
    UUID("UUID", ".//td[position() = 3]//div"),
    ACTUAL_START_TIME("Фактическое время старта", ".//td[position() = 4]"),
    ACTUAL_END_TIME("Фактическое время завершения", ".//td[position() = 5]"),
    STATUS("Статус", ".//td[position() = 6]//div//span"),
    OPERATING_MODE("Режим работы", ".//td[position() = 7]//input"),
    DOWNLOAD("Скачать", ".//td[position() = 8]//button");

    private final String name;
    private final String xPath;
}
