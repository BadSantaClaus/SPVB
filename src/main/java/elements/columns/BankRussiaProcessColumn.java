package elements.columns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BankRussiaProcessColumn implements IHasColumnDescriptor {

    PROCESS_NAME("Название процесса", ".//td[position() = 1]//div"),
    UUID("UUID", "td/div"),
    PROCESS_CREATION_TIME("Время создания процесса", ".//td[position() = 3]//div"),
    PLANNED_START_TIME("Планируемое время старта", ".//td[position() = 4]"),
    ACTUAL_START_TIME("Фактическое время старта", ".//td[position() = 5]"),
    PLANNED_END_TIME("Планируемое время завершения", ".//td[position() = 6]"),
    ACTUAL_END_TIME("Фактическое время завершения", ".//td[position() = 7]"),
    STATUS("Статус", ".//td[position() = 8]//span"),
    OPERATING_MODE("Режим работы",  ".//td[position() = 9]//span[contains(@class, 'MuiSwitch-switchBase')]");

    private final String name;
    private final String xPath;
}
